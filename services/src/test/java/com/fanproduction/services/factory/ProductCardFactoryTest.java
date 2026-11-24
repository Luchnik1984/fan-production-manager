package com.fanproduction.services.factory;

import com.fanproduction.core.entity.product.BaseProductCard;
import com.fanproduction.core.entity.product.DynamicCardEntity;
import com.fanproduction.core.entity.product.MotorCardEntity;
import com.fanproduction.core.entity.product.MotorWheelCardEntity;
import com.fanproduction.core.enums.CardTemplateType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Юнит-тесты {@link ProductCardFactory} (US10.5).
 * <p>
 * Проверяем, что после перехода {@code CardTemplateType} 7 → 5
 * каждому значению enum соответствует ровно одна сущность, а созданные
 * карточки получают правильный дискриминатор {@code card_type}.
 */
@DisplayName("ProductCardFactory")
class ProductCardFactoryTest {

    private final ProductCardFactory factory = new ProductCardFactory();

    @ParameterizedTest
    @EnumSource(CardTemplateType.class)
    @DisplayName("Каждому типу соответствует своя сущность")
    void everyTypeIsSupported(CardTemplateType type) {
        BaseProductCard card = factory.createCard(type, Map.of());

        assertThat(card).isNotNull();
        assertThat(card.getCardType()).isEqualTo(type.name());
    }

    @Test
    @DisplayName("DYNAMIC создаёт DynamicCardEntity")
    void dynamicCreatesDynamicCard() {
        BaseProductCard card = factory.createCard(CardTemplateType.DYNAMIC, Map.of());

        assertThat(card).isInstanceOf(DynamicCardEntity.class);
    }

    @Test
    @DisplayName("Системные типы создают свои сущности")
    void systemTypesCreateOwnEntities() {
        assertThat(factory.createCard(CardTemplateType.MOTOR, Map.of()))
                .isInstanceOf(MotorCardEntity.class);
        assertThat(factory.createCard(CardTemplateType.MOTOR_WHEEL, Map.of()))
                .isInstanceOf(MotorWheelCardEntity.class);
    }

    @Test
    @DisplayName("Поля из Map применяются к карточке")
    void appliesFields() {
        Map<String, Object> fields = new HashMap<>();
        fields.put("series", "4A");
        fields.put("poles", 4);
        fields.put("powerKw", 1.5);

        BaseProductCard card = factory.createCard(CardTemplateType.MOTOR, fields);

        assertThat(card).isInstanceOfSatisfying(MotorCardEntity.class, motor -> {
            assertThat(motor.getSeries()).isEqualTo("4A");
            assertThat(motor.getPoles()).isEqualTo(4);
            assertThat(motor.getPowerKw()).isEqualTo(1.5);
        });
    }

    @Test
    @DisplayName("applyFields не сбрасывает поля, которых нет в Map")
    void applyFieldsKeepsMissingValues() {
        MotorCardEntity card = new MotorCardEntity();
        card.setSeries("4A");

        factory.applyFields(card, Map.of("poles", 2));

        assertThat(card.getSeries()).isEqualTo("4A");
        assertThat(card.getPoles()).isEqualTo(2);
    }

    @Test
    @DisplayName("applyFields игнорирует неизвестные ключи и не падает")
    void applyFieldsIgnoresUnknownKeys() {
        DynamicCardEntity card = new DynamicCardEntity();

        factory.applyFields(card, Map.of("noSuchField", "value"));

        assertThat(card.getCardType()).isNull();
    }
}
