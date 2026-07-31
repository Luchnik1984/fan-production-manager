package com.fanproduction.gui.configurator;

import javafx.scene.control.Alert;

import java.util.Map;

/**
 * Хелпер для валидации полей, влияющих на формулу колеса.
 * Используется в RadialWheelCardConfigurator и AxialWheelCardConfigurator.
 */
public class WheelFormulaValidator {

    /**
     * Проверяет, что все поля для формирования формулы радиального колеса заполнены.
     *
     * @param fields карта полей со значениями
     * @param isOwn  true если выбрано "Фирменное рабочее колесо"
     * @return true если все поля заполнены, false если есть ошибки
     */
    public static boolean validateRadialWheelFormula(Map<String, Object> fields, boolean isOwn) {
        // Если не фирменное — формула не нужна
        if (!isOwn) {
            return true;
        }

        StringBuilder errors = new StringBuilder();

        String bladeMod = (String) fields.get("bladeMod");
        String frontDiskMod = (String) fields.get("frontDiskMod");
        Double wheelWidth = (Double) fields.get("wheelWidth");
        Integer bladeCount = (Integer) fields.get("bladeCount");
        Double bladeLengthCoeff = (Double) fields.get("bladeLengthCoeff");

        if (bladeMod == null || bladeMod.isEmpty()) {
            errors.append("""
                    - Модификация лопатки
                    """);
        }
        if (frontDiskMod == null || frontDiskMod.isEmpty()) {
            errors.append("""
                    - Модификация переднего диска
                    """);
        }
        if (wheelWidth == null) {
            errors.append("""
                    - Ширина колеса
                    """);
        }
        if (bladeCount == null) {
            errors.append("""
                    - Количество лопаток
                    """);
        }
        if (bladeLengthCoeff == null) {
            errors.append("""
                    - Коэффициент длины лопатки
                    """);
        }

        if (!errors.isEmpty()) {
            showError(
                    """
                            Для формирования формулы колеса необходимо заполнить следующие поля:
                            """ +
                            errors
            );
            return false;
        }

        return true;
    }

    /**
     * Проверяет, что все поля для формирования формулы осевого колеса заполнены.
     *
     * @param fields карта полей со значениями
     * @param isOwn  true если выбрано "Фирменное рабочее колесо"
     * @return true если все поля заполнены, false если есть ошибки
     */
    public static boolean validateAxialWheelFormula(Map<String, Object> fields, boolean isOwn) {
        // Если не фирменное — формула не нужна
        if (!isOwn) {
            return true;
        }

        boolean isAssembled = Boolean.TRUE.equals(fields.get("isAssembledFromComponents"));
        boolean isWelded = Boolean.TRUE.equals(fields.get("isWeldedFromMaterials"));

        // Если не выбран тип изготовления — формула не нужна
        if (!isAssembled && !isWelded) {
            return true;
        }

        StringBuilder errors = new StringBuilder();

        // Общие поля для обоих типов
        Integer bladeCount = (Integer) fields.get("bladeCount");
        Integer maxBladeCount = (Integer) fields.get("maxBladeCount");
        Integer bladeAngle = (Integer) fields.get("bladeAngle");
        String bladeMaterial = (String) fields.get("bladeMaterial");

        if (bladeCount == null) {
            errors.append("""
                    - Количество установленных лопаток
                    """);
        }
        if (maxBladeCount == null) {
            errors.append("""
                    - Максимальное количество лопаток для данного Хаба
                    """);
        }
        if (bladeAngle == null) {
            errors.append("""
                    - Угол установки лопаток
                    """);
        }
        if (bladeMaterial == null || bladeMaterial.isEmpty()) {
            errors.append("""
                    - Материал лопатки
                    """);
        }

        // Поля для сборного колеса
        if (isAssembled) {
            Long wheelHubComponentId = (Long) fields.get("wheelHubComponentId");
            Long bladeComponentId = (Long) fields.get("bladeComponentId");
            Long hubComponentId = (Long) fields.get("hubComponentId");

            if (wheelHubComponentId == null) {
                errors.append("""
                        - Хаб (Ступица) рабочего колеса
                        """);
            }
            if (bladeComponentId == null) {
                errors.append("""
                        - Лопатка рабочего колеса
                        """);
            }
            if (hubComponentId == null) {
                errors.append("""
                        - Установочная ступица
                        """);
            }
        }

        // Поля для сварного колеса
        if (isWelded) {
            String wheelHubType = (String) fields.get("wheelHubType");
            String bladeType = (String) fields.get("bladeType");

            if (wheelHubType == null || wheelHubType.isEmpty()) {
                errors.append("""
                        - Тип хаба (вручную)
                        """);
            }
            if (bladeType == null || bladeType.isEmpty()) {
                errors.append("""
                        - Тип лопатки (вручную)
                        """);
            }
        }

        if (!errors.isEmpty()) {
            showError(
                    """
                            Для формирования формулы колеса необходимо заполнить следующие поля:
                            """ +
                    errors
            );
            return false;
        }

        return true;
    }

    private static void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

