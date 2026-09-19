package org.zafu.news.client;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.zafu.news.exception.RssFetchException;
import org.zafu.news.exception.RssTimeoutException;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Flow;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Component
public class RssClient {
    private static final int MAX_BYTES = 2 * 1024 * 1024;
    private final HttpClient httpClient;
    @Getter
    private final URI feedUrl;
    private final Duration requestTimeout;

    @Autowired
    public RssClient(HttpClient rssHttpClient, @Value("${app.ingestion.feed-url}") URI feedUrl) {
        this(rssHttpClient, feedUrl, Duration.ofSeconds(15));
    }

    RssClient(HttpClient httpClient, URI feedUrl, Duration requestTimeout) {
        this.httpClient = httpClient;
        this.feedUrl = feedUrl;
        this.requestTimeout = requestTimeout;
    }

    public byte[] fetch() {
        var request = HttpRequest.newBuilder(feedUrl).timeout(requestTimeout)
                .header("Accept", "application/rss+xml, application/xml, text/xml")
                .GET().build();
        var responseFuture = httpClient.sendAsync(request, responseInfo -> {
            if (responseInfo.statusCode() != 200) {
                throw new RssFetchException("RSS source returned HTTP " + responseInfo.statusCode());
            }
            return new LimitedBodySubscriber();
        });
        try {
            return responseFuture.get(requestTimeout.toMillis(), TimeUnit.MILLISECONDS).body();
        } catch (TimeoutException exception) {
            responseFuture.cancel(true);
            throw new RssTimeoutException(exception);
        } catch (InterruptedException exception) {
            responseFuture.cancel(true);
            Thread.currentThread().interrupt();
            throw new RssFetchException("RSS request was interrupted", exception);
        } catch (ExecutionException exception) {
            Throwable cause = exception.getCause();
            if (cause instanceof HttpTimeoutException) {
                throw new RssTimeoutException(cause);
            }
            if (cause instanceof RssFetchException rssException) {
                throw rssException;
            }
            throw new RssFetchException("Unable to fetch RSS source", cause);
        }
    }

    private static class LimitedBodySubscriber implements HttpResponse.BodySubscriber<byte[]> {
        private final HttpResponse.BodySubscriber<byte[]> delegate = HttpResponse.BodySubscribers.ofByteArray();
        private Flow.Subscription subscription;
        private long receivedBytes;

        @Override
        public CompletionStage<byte[]> getBody() {
            return delegate.getBody();
        }

        @Override
        public void onSubscribe(Flow.Subscription subscription) {
            this.subscription = subscription;
            delegate.onSubscribe(subscription);
        }

        @Override
        public void onNext(List<ByteBuffer> buffers) {
            for (ByteBuffer buffer : buffers) {
                receivedBytes += buffer.remaining();
            }
            if (receivedBytes > MAX_BYTES) {
                subscription.cancel();
                delegate.onError(new RssFetchException("RSS source exceeds the 2 MiB limit"));
                return;
            }
            delegate.onNext(buffers);
        }

        @Override
        public void onError(Throwable throwable) {
            delegate.onError(throwable);
        }

        @Override
        public void onComplete() {
            delegate.onComplete();
        }
    }
}
