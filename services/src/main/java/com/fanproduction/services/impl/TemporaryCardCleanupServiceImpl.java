package com.fanproduction.services.impl;

import com.fanproduction.core.entity.product.BaseProductCard;
import com.fanproduction.repositories.product.ProductCardRepository;
import com.fanproduction.services.TemporaryCardCleanupService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TemporaryCardCleanupServiceImpl implements TemporaryCardCleanupService {

    private final ProductCardRepository productCardRepository;

    @Override
    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void cleanupOldTemporaryCards() {
        System.out.println("=== TemporaryCardCleanupService: cleanup started ===");
        new Thread(() -> {
            try {
                List<BaseProductCard> tempCards = productCardRepository.findByIsTemporaryTrue();
                int deletedCount = 0;
                for (BaseProductCard card : tempCards) {
                    productCardRepository.delete(card);
                    deletedCount++;
                    System.out.println("Cleaned up orphaned temporary card: " + card.getId() + " - " + card.getName());
                }
                if (deletedCount > 0) {
                    System.out.println("Total temporary cards cleaned up: " + deletedCount);
                }
            } catch (Exception e) {
                System.err.println("Failed to cleanup temporary cards: " + e.getMessage());
            }
        }).start();
    }
}