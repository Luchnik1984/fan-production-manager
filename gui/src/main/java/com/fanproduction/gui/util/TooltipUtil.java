package com.fanproduction.gui.util;

import com.fanproduction.gui.component.EditableTooltipCell;
import javafx.scene.control.TableCell;
import javafx.scene.control.Tooltip;
import javafx.util.Duration;
import javafx.util.StringConverter;

public class TooltipUtil {

    /**
     * Создаёт ячейку с всплывающей подсказкой (только для чтения)
     */
    public static <S> TableCell<S, String> createTooltipCell() {
        return new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setTooltip(null);
                } else {
                    setText(item);
                    if (!item.isEmpty()) {
                        Tooltip tooltip = new Tooltip(item);
                        tooltip.setShowDelay(Duration.millis(100));
                        tooltip.setShowDuration(Duration.INDEFINITE);
                        setTooltip(tooltip);
                    }
                }
            }
        };
    }

    /**
     * Создаёт редактируемую ячейку с всплывающей подсказкой для строк
     */
    public static <S> TableCell<S, String> createEditableStringTooltipCell() {
        return new EditableTooltipCell<>(new StringConverter<>() {
            @Override
            public String toString(String object) {
                return object == null ? "" : object;
            }
            @Override
            public String fromString(String string) {
                return string;
            }
        });
    }

    /**
     * Создаёт редактируемую ячейку с всплывающей подсказкой для Double
     */
    public static <S> TableCell<S, Double> createEditableDoubleTooltipCell() {
        return new EditableTooltipCell<>(new StringConverter<>() {
            @Override
            public String toString(Double object) {
                return object == null ? "" : String.valueOf(object);
            }
            @Override
            public Double fromString(String string) {
                try {
                    return Double.parseDouble(string);
                } catch (NumberFormatException e) {
                    return 0.0;
                }
            }
        });
    }
}
