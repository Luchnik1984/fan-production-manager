package com.fanproduction.services.factory;

import com.fanproduction.core.entity.BaseProductCard;

@FunctionalInterface
public interface CardSupplier {
    BaseProductCard get();
}
