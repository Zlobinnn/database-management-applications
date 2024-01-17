import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MarksCollector {
    private static String JDBC_URL = "";
    private static String DB_USER = "";
    private static String DB_PASSWORD = "";

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            AuthenticationDialog authDialog = new AuthenticationDialog(null);
            authDialog.setVisible(true);

            if (authDialog.isAuthenticated()) {
                JDBC_URL = authDialog.getJdbcUrl();
                DB_USER = authDialog.getDbUser();
                DB_PASSWORD = authDialog.getDbPassword();
                createAndShowGUI();
            } else {
                JOptionPane.showMessageDialog(null, "Аутентификация отменена", "Ошибка аутентификации", JOptionPane.ERROR_MESSAGE);
                System.exit(0);
            }
        });
    }

    private static void createAndShowGUI() {
        JFrame frame = new JFrame("Приложение коллекции марок");
        frame.setSize(400, 350);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(9, 1));

        JButton addMarkButton = new JButton("Добавить новую марку");
        JButton addThemeButton = new JButton("Добавить новую тему");
        JButton removeThemeButton = new JButton("Удалить тему");
        JButton changeLocationButton = new JButton("Изменить месторасположение марки");
        JButton getCountryBySectionButton = new JButton("Получить список стран в разделе");
        JButton getVolumeBySeriesButton = new JButton("Получить том по серии");
        JButton getThemesBySizeButton = new JButton("Получить темы по размеру марки");
        JButton getCountryByLocationButton = new JButton("Получить название страны марки в определённом месте");
        JButton getLocationsByThemeButton = new JButton("Получить месторасположения марок по теме");

        panel.add(addMarkButton);
        panel.add(addThemeButton);
        panel.add(removeThemeButton);
        panel.add(changeLocationButton);
        panel.add(getCountryBySectionButton);
        panel.add(getVolumeBySeriesButton);
        panel.add(getThemesBySizeButton);
        panel.add(getCountryByLocationButton);
        panel.add(getLocationsByThemeButton);


        frame.add(panel);

        addMarkButton.addActionListener(e -> openAddMarkDialog());
        addThemeButton.addActionListener(e -> openAddThemeDialog());
        removeThemeButton.addActionListener(e -> openRemoveThemeDialog());
        changeLocationButton.addActionListener(e -> openChangeLocationDialog());
        getCountryBySectionButton.addActionListener(e -> openGetCountryBySectionDialog());
        getVolumeBySeriesButton.addActionListener(e -> openGetVolumeBySeriesDialog());
        getThemesBySizeButton.addActionListener(e -> openGetThemesBySizeDialog());
        getCountryByLocationButton.addActionListener(e -> openGetCountryByLocationDialog());
        getLocationsByThemeButton.addActionListener(e -> openGetLocationsByThemeDialog());

        frame.setVisible(true);
    }
    private static void openGetLocationsByThemeDialog() {
        JFrame getLocationsByThemeFrame = new JFrame("Получить расположения по теме");
        getLocationsByThemeFrame.setSize(300, 150);

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(3, 2));

        JComboBox<String> themeNameComboBox = new JComboBox<>(getThemes());

        panel.add(new JLabel("Выберите название темы:"));
        panel.add(themeNameComboBox);

        JButton getButton = new JButton("Получить месторасположения");
        getButton.addActionListener(e -> {
            String selectedThemeName = (String) themeNameComboBox.getSelectedItem();
            if (selectedThemeName != null) {
                try (Connection connection = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASSWORD)) {
                    getLocationsByTheme(connection, selectedThemeName);

                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            getLocationsByThemeFrame.dispose();
        });

        panel.add(getButton);

        getLocationsByThemeFrame.add(panel);
        getLocationsByThemeFrame.setVisible(true);
    }


    private static void getLocationsByTheme(Connection connection, String themeName) throws SQLException {
        String query = "SELECT SectionNumber, VolumeNumber, PageNumber, PositionOnPage " +
                "FROM CollectionLocation CL " +
                "JOIN MarkThemeAssociation MTA ON CL.MarkID = MTA.MarkID " +
                "JOIN CollectionThemes CT ON MTA.ThemeID = CT.ThemeID " +
                "WHERE CT.ThemeName = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, themeName);

            ResultSet resultSet = preparedStatement.executeQuery();
            StringBuilder result = new StringBuilder("Месторасположения марок по теме '" + themeName + "':\n");
            while (resultSet.next()) {
                result.append("Раздел: ").append(resultSet.getInt("SectionNumber"))
                        .append(", Том: ").append(resultSet.getInt("VolumeNumber"))
                        .append(", Страница: ").append(resultSet.getInt("PageNumber"))
                        .append(", Позиция: ").append(resultSet.getInt("PositionOnPage"))
                        .append("\n");
            }

            JOptionPane.showMessageDialog(null, result.toString(), "Расположения по теме", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private static void openGetVolumeBySeriesDialog() {
        JFrame getVolumeBySeriesFrame = new JFrame("Получить том по серии");
        getVolumeBySeriesFrame.setSize(250, 150);

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(3, 2));

        JComboBox<String> seriesThemeComboBox = new JComboBox<>(getThemes());

        panel.add(new JLabel("Выберите тему серии:"));
        panel.add(seriesThemeComboBox);

        JButton getButton = new JButton("Получить том");
        getButton.addActionListener(e -> {
            String selectedSeriesTheme = (String) seriesThemeComboBox.getSelectedItem();
            if (selectedSeriesTheme != null) {
                try (Connection connection = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASSWORD)) {
                    getVolumeBySeries(connection, selectedSeriesTheme);

                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            getVolumeBySeriesFrame.dispose();
        });

        panel.add(getButton);

        getVolumeBySeriesFrame.add(panel);
        getVolumeBySeriesFrame.setVisible(true);
    }

    private static void getVolumeBySeries(Connection connection, String seriesTheme) throws SQLException {
        String query = "SELECT DISTINCT CL.SectionNumber FROM CollectionLocation CL " +
                "JOIN MarkThemeAssociation MTA ON CL.MarkID = MTA.MarkID " +
                "JOIN CollectionThemes CT ON MTA.ThemeID = CT.ThemeID " +
                "WHERE CT.ThemeName = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, seriesTheme);

            ResultSet resultSet = preparedStatement.executeQuery();
            StringBuilder result = new StringBuilder("Том, содержащий тему серии '" + seriesTheme + "':\n");
            while (resultSet.next()) {
                result.append(resultSet.getInt("SectionNumber")).append("\n");
            }

            JOptionPane.showMessageDialog(null, result.toString(), "Том по серии", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private static void openGetThemesBySizeDialog() {
        JFrame getThemesBySizeFrame = new JFrame("Получить темы по размеру");
        getThemesBySizeFrame.setSize(250, 150);

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(3, 2));

        JComboBox<String> markSizeComboBox = new JComboBox<>(getMarkSizes());

        panel.add(new JLabel("Выберите размер марки:"));
        panel.add(markSizeComboBox);

        JButton getButton = new JButton("Получить тему");
        getButton.addActionListener(e -> {
            String selectedMarkSize = (String) markSizeComboBox.getSelectedItem();
            if (selectedMarkSize != null) {
                try (Connection connection = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASSWORD)) {
                    getThemesBySize(connection, selectedMarkSize);

                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            getThemesBySizeFrame.dispose();
        });

        panel.add(getButton);

        getThemesBySizeFrame.add(panel);
        getThemesBySizeFrame.setVisible(true);
    }

    private static void getThemesBySize(Connection connection, String markSize) throws SQLException {
        String query = "SELECT DISTINCT CT.ThemeName FROM CollectionThemes CT " +
                "JOIN MarkThemeAssociation MTA ON CT.ThemeID = MTA.ThemeID " +
                "WHERE MTA.MarkID IN (SELECT MarkID FROM Marks WHERE MarkSize = ?)";

        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, markSize);

            ResultSet resultSet = preparedStatement.executeQuery();
            StringBuilder result = new StringBuilder("Темы с марками размера '" + markSize + "':\n");
            while (resultSet.next()) {
                result.append(resultSet.getString("ThemeName")).append("\n");
            }

            JOptionPane.showMessageDialog(null, result.toString(), "Темы по размеру", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private static void openGetCountryByLocationDialog() {
        JFrame getCountryByLocationFrame = new JFrame("Получить страну по месторасположению");
        getCountryByLocationFrame.setSize(300, 150);

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(5, 2));

        JTextField sectionNumberField = new JTextField();
        JTextField volumeNumberField = new JTextField();
        JTextField pageNumberField = new JTextField();
        JTextField positionOnPageField = new JTextField();

        panel.add(new JLabel("Номер раздела:"));
        panel.add(sectionNumberField);
        panel.add(new JLabel("Номер тома:"));
        panel.add(volumeNumberField);
        panel.add(new JLabel("Номер страницы:"));
        panel.add(pageNumberField);
        panel.add(new JLabel("Положение на странице:"));
        panel.add(positionOnPageField);

        JButton getButton = new JButton("Получить страну");
        getButton.addActionListener(e -> {
            String sectionNumber = sectionNumberField.getText();
            String volumeNumber = volumeNumberField.getText();
            String pageNumber = pageNumberField.getText();
            String positionOnPage = positionOnPageField.getText();
            if (!sectionNumber.isEmpty() && !pageNumber.isEmpty()) {
                try (Connection connection = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASSWORD)) {
                    getCountryByLocation(connection, sectionNumber, volumeNumber, pageNumber, positionOnPage);

                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            getCountryByLocationFrame.dispose();
        });

        panel.add(getButton);

        getCountryByLocationFrame.add(panel);
        getCountryByLocationFrame.setVisible(true);
    }

    private static void getCountryByLocation(Connection connection, String sectionNumber, String volumeNumber, String pageNumber, String positionOnPage) throws SQLException {
        String query = "SELECT DISTINCT C.CountryName FROM CollectionLocation CL " +
                "JOIN MarkCountryAssociation MCA ON CL.MarkID = MCA.MarkID " +
                "JOIN Countries C ON MCA.CountryID = C.CountryID " +
                "WHERE CL.SectionNumber = ? AND CL.VolumeNumber = ? AND CL.PageNumber = ? AND CL.PositionOnPage = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, Integer.parseInt(sectionNumber));
            preparedStatement.setInt(2, Integer.parseInt(volumeNumber));
            preparedStatement.setInt(3, Integer.parseInt(pageNumber));
            preparedStatement.setInt(4, Integer.parseInt(positionOnPage));

            ResultSet resultSet = preparedStatement.executeQuery();
            StringBuilder result = new StringBuilder("Страна в разделе " + sectionNumber +
                    ", том " + volumeNumber +
                    ", страница " + pageNumber +
                    ", позиция " + positionOnPage + ":\n");

            while (resultSet.next()) {
                result.append(resultSet.getString("CountryName")).append("\n");
            }

            JOptionPane.showMessageDialog(null, result.toString(), "Страна по месторасположению", JOptionPane.INFORMATION_MESSAGE);
        }
    }


    private static void openGetCountryBySectionDialog() {
        JFrame getCountryBySectionFrame = new JFrame("Получить страны в разделе");
        getCountryBySectionFrame.setSize(250, 150);

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(3, 2));

        JComboBox<String> sectionComboBox = new JComboBox<>(getSections());

        panel.add(new JLabel("Выберите раздел:"));
        panel.add(sectionComboBox);

        JButton getButton = new JButton("Получить страны");
        getButton.addActionListener(e -> {
            String selectedSection = (String) sectionComboBox.getSelectedItem();
            if (selectedSection != null) {
                try (Connection connection = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASSWORD)) {
                    getCountriesBySection(connection, selectedSection);

                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            getCountryBySectionFrame.dispose();
        });

        panel.add(getButton);

        getCountryBySectionFrame.add(panel);
        getCountryBySectionFrame.setVisible(true);
    }

    private static String[] getSections() {
        List<String> sections = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASSWORD)) {
            String query = "SELECT DISTINCT SectionNumber FROM CollectionLocation";
            try (Statement statement = connection.createStatement();
                 ResultSet resultSet = statement.executeQuery(query)) {
                while (resultSet.next()) {
                    sections.add(String.valueOf(resultSet.getInt("SectionNumber")));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return sections.toArray(new String[0]);
    }

    private static void getCountriesBySection(Connection connection, String sectionNumber) throws SQLException {
        String query = "SELECT DISTINCT C.CountryName FROM CollectionLocation CL " +
                "JOIN MarkCountryAssociation MCA ON CL.MarkID = MCA.MarkID " +
                "JOIN Countries C ON MCA.CountryID = C.CountryID " +
                "WHERE CL.SectionNumber = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, Integer.parseInt(sectionNumber));

            ResultSet resultSet = preparedStatement.executeQuery();
            StringBuilder result = new StringBuilder("Страны в разделе " + sectionNumber + ":\n");
            while (resultSet.next()) {
                result.append(resultSet.getString("CountryName")).append("\n");
            }

            JOptionPane.showMessageDialog(null, result.toString(), "Страны в разделе", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private static void openAddMarkDialog() {
        JFrame addMarkFrame = new JFrame("Добавить новую марку");
        addMarkFrame.setSize(300, 200);

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(6, 2));

        JTextField markIdField = new JTextField();
        JTextField countryField = new JTextField();
        JTextField seriesNumberField = new JTextField();
        JTextField seriesThemeField = new JTextField();
        JTextField yearReleasedField = new JTextField();
        JTextField markColorField = new JTextField();
        JTextField markSizeField = new JTextField();
        JTextField markPriceField = new JTextField();
        JTextField markThemeField = new JTextField();

        panel.add(new JLabel("ID марки:"));
        panel.add(markIdField);
        panel.add(new JLabel("Страна:"));
        panel.add(countryField);
        panel.add(new JLabel("Номер серии:"));
        panel.add(seriesNumberField);
        panel.add(new JLabel("Тема серии:"));
        panel.add(seriesThemeField);
        panel.add(new JLabel("Год выпуска марки:"));
        panel.add(yearReleasedField);
        panel.add(new JLabel("Цвет марки:"));
        panel.add(markColorField);
        panel.add(new JLabel("Размер марки:"));
        panel.add(markSizeField);
        panel.add(new JLabel("Цена марки:"));
        panel.add(markPriceField);
        panel.add(new JLabel("Тема марки:"));
        panel.add(markThemeField);

        JButton addButton = new JButton("Добавить марку");
        addButton.addActionListener(e -> {
            try (Connection connection = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASSWORD)) {
                addMark(connection,
                        markIdField.getText(),
                        countryField.getText(),
                        seriesNumberField.getText(),
                        seriesThemeField.getText(),
                        yearReleasedField.getText(),
                        markColorField.getText(),
                        markSizeField.getText(),
                        markPriceField.getText(),
                        markThemeField.getText());

            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            addMarkFrame.dispose();
        });

        panel.add(addButton);

        addMarkFrame.add(panel);
        addMarkFrame.setVisible(true);
    }

    private static void addMark(Connection connection, String markId, String country, String seriesNumber,
                                String seriesTheme, String yearReleased, String markColor, String markSize,
                                String markPrice, String markTheme) throws SQLException {
        String insertMarkQuery = "INSERT INTO Marks (MarkID, Country, SeriesNumber, SeriesTheme, YearReleased, " +
                "MarkColor, MarkSize, MarkPrice, MarkTheme) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement preparedStatement = connection.prepareStatement(insertMarkQuery)) {
            preparedStatement.setInt(1, Integer.parseInt(markId));
            preparedStatement.setString(2, country);
            preparedStatement.setInt(3, Integer.parseInt(seriesNumber));
            preparedStatement.setString(4, seriesTheme);
            preparedStatement.setInt(5, Integer.parseInt(yearReleased));
            preparedStatement.setString(6, markColor);
            preparedStatement.setString(7, markSize);
            preparedStatement.setDouble(8, Double.parseDouble(markPrice));
            preparedStatement.setString(9, markTheme);

            preparedStatement.executeUpdate();
        }
    }

    private static void openAddThemeDialog() {
        JFrame addThemeFrame = new JFrame("Добавить новую тему");
        addThemeFrame.setSize(300, 150);

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(3, 2));

        JTextField themeIdField = new JTextField();
        JTextField themeNameField = new JTextField();

        panel.add(new JLabel("ID темы:"));
        panel.add(themeIdField);
        panel.add(new JLabel("Название темы:"));
        panel.add(themeNameField);

        JButton addButton = new JButton("Добавить тему");
        addButton.addActionListener(e -> {
            try (Connection connection = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASSWORD)) {
                addTheme(connection,
                        themeIdField.getText(),
                        themeNameField.getText());

            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            addThemeFrame.dispose();
        });

        panel.add(addButton);

        addThemeFrame.add(panel);
        addThemeFrame.setVisible(true);
    }

    private static void addTheme(Connection connection, String themeId, String themeName) throws SQLException {
        String insertThemeQuery = "INSERT INTO CollectionThemes (ThemeID, ThemeName) VALUES (?, ?)";

        try (PreparedStatement preparedStatement = connection.prepareStatement(insertThemeQuery)) {
            preparedStatement.setInt(1, Integer.parseInt(themeId));
            preparedStatement.setString(2, themeName);

            preparedStatement.executeUpdate();
        }
    }

    private static void openRemoveThemeDialog() {
        JFrame removeThemeFrame = new JFrame("Удалить тему");
        removeThemeFrame.setSize(250, 150);

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(3, 2));

        JComboBox<String> themeComboBox = new JComboBox<>(getThemes());

        panel.add(new JLabel("Выберите тему для удаления:"));
        panel.add(themeComboBox);

        JButton removeButton = new JButton("Удалить тему");
        removeButton.addActionListener(e -> {
            try (Connection connection = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASSWORD)) {
                String selectedTheme = (String) themeComboBox.getSelectedItem();
                removeTheme(connection, selectedTheme);

            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            removeThemeFrame.dispose();
        });

        panel.add(removeButton);

        removeThemeFrame.add(panel);
        removeThemeFrame.setVisible(true);
    }

    private static void removeTheme(Connection connection, String themeName) throws SQLException {
        String deleteMarksQuery = "DELETE FROM Marks WHERE ThemeName = ?";
        try (PreparedStatement deleteMarksStatement = connection.prepareStatement(deleteMarksQuery)) {
            deleteMarksStatement.setString(1, themeName);
            deleteMarksStatement.executeUpdate();
        }

        String deleteLocationsQuery = "DELETE FROM CollectionLocation WHERE MarkID NOT IN (SELECT MarkID FROM Marks)";
        try (PreparedStatement deleteLocationsStatement = connection.prepareStatement(deleteLocationsQuery)) {
            deleteLocationsStatement.executeUpdate();
        }

        String deleteThemeQuery = "DELETE FROM CollectionThemes WHERE ThemeName = ?";
        try (PreparedStatement deleteThemeStatement = connection.prepareStatement(deleteThemeQuery)) {
            deleteThemeStatement.setString(1, themeName);
            deleteThemeStatement.executeUpdate();
        }
    }

    private static void openChangeLocationDialog() {
        JFrame changeLocationFrame = new JFrame("Поменять месторасположение марки");
        changeLocationFrame.setSize(300, 150);

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(6, 2));

        JTextField markIdField = new JTextField();
        JTextField sectionNumberField = new JTextField();
        JTextField volumeNumberField = new JTextField();
        JTextField pageNumberField = new JTextField();
        JTextField positionOnPageField = new JTextField();

        panel.add(new JLabel("ID марки:"));
        panel.add(markIdField);
        panel.add(new JLabel("Новый раздел марки:"));
        panel.add(sectionNumberField);
        panel.add(new JLabel("Новый том марки:"));
        panel.add(volumeNumberField);
        panel.add(new JLabel("Новая страница марки:"));
        panel.add(pageNumberField);
        panel.add(new JLabel("Новое расположение на странице:"));
        panel.add(positionOnPageField);

        JButton changeButton = new JButton("Изменить расположение");
        changeButton.addActionListener(e -> {
            try (Connection connection = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASSWORD)) {
                changeMarkLocation(connection,
                        markIdField.getText(),
                        sectionNumberField.getText(),
                        volumeNumberField.getText(),
                        pageNumberField.getText(),
                        positionOnPageField.getText());
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            changeLocationFrame.dispose();
        });

        panel.add(changeButton);

        changeLocationFrame.add(panel);
        changeLocationFrame.setVisible(true);
    }

    private static void changeMarkLocation(Connection connection, String markId, String sectionNumber, String volumeNumber, String pageNumber, String positionOnPage) throws SQLException {
        String updateLocationQuery = "UPDATE CollectionLocation SET SectionNumber = ?, VolumeNumber = ?, PageNumber = ?, PositionOnPage = ? WHERE MarkID = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(updateLocationQuery)) {
            preparedStatement.setInt(1, Integer.parseInt(sectionNumber));
            preparedStatement.setInt(2, Integer.parseInt(volumeNumber));
            preparedStatement.setInt(3, Integer.parseInt(pageNumber));
            preparedStatement.setInt(4, Integer.parseInt(positionOnPage));
            preparedStatement.setInt(5, Integer.parseInt(markId));

            preparedStatement.executeUpdate();
        }
    }
    private static String[] getThemes() {
        List<String> themes = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASSWORD)) {
            String query = "SELECT DISTINCT ThemeName FROM CollectionThemes";
            try (Statement statement = connection.createStatement();
                 ResultSet resultSet = statement.executeQuery(query)) {
                while (resultSet.next()) {
                    themes.add(resultSet.getString("ThemeName"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return themes.toArray(new String[0]);
    }

    private static String[] getMarkSizes() {
        List<String> markSizes = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASSWORD)) {
            String query = "SELECT DISTINCT MarkSize FROM Marks";
            try (Statement statement = connection.createStatement();
                 ResultSet resultSet = statement.executeQuery(query)) {
                while (resultSet.next()) {
                    markSizes.add(resultSet.getString("MarkSize"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return markSizes.toArray(new String[0]);
    }
}

