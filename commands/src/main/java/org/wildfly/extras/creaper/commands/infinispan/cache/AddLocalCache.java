package org.wildfly.extras.creaper.commands.infinispan.cache;

import org.wildfly.extras.creaper.core.ServerVersion;
import org.wildfly.extras.creaper.core.online.operations.Values;

public final class AddLocalCache extends AbstractAddCache {
    private AddLocalCache(Builder builder) {
        super(builder, CacheType.LOCAL_CACHE);
    }

    @Override
    protected Values addValuesSpecificForCacheType(Values generalCacheValues, ServerVersion version) {
        return generalCacheValues;
    }

    public static final class Builder extends AbstractAddCache.Builder<Builder> {
        public Builder(String name) {
            super(name);
        }

        public AddLocalCache build() {
            return new AddLocalCache(this);
        }
    }
}
