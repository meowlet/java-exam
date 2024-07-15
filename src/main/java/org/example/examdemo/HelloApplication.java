package org.example.examdemo;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class HelloApplication extends Application {

    private TextField txtProductId, txtProductName, txtProductPrice, txtProductQuantity, txtProductStock;
    private ImageView imageView;
    private Label lblError;
    private TableView<Product> tableView;
    private String selectedImagePath;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        VBox root = new VBox(10);
        root.setPadding(new Insets(10));

        GridPane form = createForm();
        root.getChildren().add(form);

        HBox buttonBox = createButtonBox();
        root.getChildren().add(buttonBox);

        lblError = new Label();
        lblError.setStyle("-fx-text-fill: red;");
        root.getChildren().add(lblError);

        tableView = createProductTable();
        root.getChildren().add(tableView);

        loadProducts();

        Scene scene = new Scene(root, 800, 600);
        stage.setScene(scene);
        stage.setTitle("Product Management");
        stage.show();
    }

    private GridPane createForm() {
        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);

        form.add(new Label("Product ID:"), 0, 0);
        txtProductId = new TextField();
        form.add(txtProductId, 1, 0);

        form.add(new Label("Product Name:"), 0, 1);
        txtProductName = new TextField();
        form.add(txtProductName, 1, 1);

        form.add(new Label("Product Price:"), 0, 2);
        txtProductPrice = new TextField();
        form.add(txtProductPrice, 1, 2);

        form.add(new Label("Product Quantity:"), 0, 3);
        txtProductQuantity = new TextField();
        form.add(txtProductQuantity, 1, 3);

        form.add(new Label("Product Stock:"), 0, 4);
        txtProductStock = new TextField();
        form.add(txtProductStock, 1, 4);

        Button btnChooseImage = new Button("Choose Image");
        btnChooseImage.setOnAction(e -> chooseImage());
        form.add(btnChooseImage, 0, 5);

        imageView = new ImageView();
        imageView.setFitHeight(100);
        imageView.setFitWidth(100);
        form.add(imageView, 1, 5);

        return form;
    }

    private HBox createButtonBox() {
        HBox buttonBox = new HBox(10);

        Button btnAdd = new Button("Add");
        btnAdd.setOnAction(e -> addProduct());

        Button btnUpdate = new Button("Update");
        btnUpdate.setOnAction(e -> updateProduct());

        Button btnDelete = new Button("Delete");
        btnDelete.setOnAction(e -> deleteProduct());

        Button btnFind = new Button("Find");
        btnFind.setOnAction(e -> findProduct());

        buttonBox.getChildren().addAll(btnAdd, btnUpdate, btnDelete, btnFind);

        return buttonBox;
    }

    private TableView<Product> createProductTable() {
        TableView<Product> table = new TableView<>();

        TableColumn<Product, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<Product, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<Product, Double> priceCol = new TableColumn<>("Price");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));

        TableColumn<Product, Integer> quantityCol = new TableColumn<>("Quantity");
        quantityCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));

        TableColumn<Product, Integer> stockCol = new TableColumn<>("Stock");
        stockCol.setCellValueFactory(new PropertyValueFactory<>("stock"));

        TableColumn<Product, String> imagePathCol = new TableColumn<>("Image Path");
        imagePathCol.setCellValueFactory(new PropertyValueFactory<>("imagePath"));

        table.getColumns().addAll(idCol, nameCol, priceCol, quantityCol, stockCol, imagePathCol);

        table.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                displayProductDetails(newSelection);
            }
        });

        return table;
    }

    private void chooseImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Image File");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.gif")
        );
        File selectedFile = fileChooser.showOpenDialog(null);

        if (selectedFile != null) {
            selectedImagePath = selectedFile.toURI().toString();
            imageView.setImage(new Image(selectedImagePath));
        }
    }

    private void addProduct() {
        try {
            String name = txtProductName.getText();
            double price = Double.parseDouble(txtProductPrice.getText());
            int quantity = Integer.parseInt(txtProductQuantity.getText());
            int stock = Integer.parseInt(txtProductStock.getText());
            int id = Integer.parseInt(txtProductId.getText());

            if (selectedImagePath == null || selectedImagePath.isEmpty()) {
                lblError.setText("Please select an image.");
                return;
            }

            String insertQuery = "INSERT INTO products (name, price, quantity, stock, image_path, id) VALUES (?, ?, ?, ?, ?, ?)";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(insertQuery)) {
                pstmt.setString(1, name);
                pstmt.setDouble(2, price);
                pstmt.setInt(3, quantity);
                pstmt.setInt(4, stock);
                pstmt.setString(5, selectedImagePath);
                pstmt.setInt(6, id);

                int affectedRows = pstmt.executeUpdate();
                if (affectedRows > 0) {
                    lblError.setText("Product added successfully.");
                    loadProducts();
                    clearForm();
                } else {
                    lblError.setText("Failed to add product.");
                }
            }
        } catch (NumberFormatException e) {
            lblError.setText("Please enter valid numbers for price, quantity, and stock.");
        } catch (SQLException e) {
            lblError.setText("Database error: " + e.getMessage());
        }
    }

    private void updateProduct() {
        try {
            int id = Integer.parseInt(txtProductId.getText());
            String name = txtProductName.getText();
            double price = Double.parseDouble(txtProductPrice.getText());
            int quantity = Integer.parseInt(txtProductQuantity.getText());
            int stock = Integer.parseInt(txtProductStock.getText());

            String updateQuery = "UPDATE products SET name = ?, price = ?, quantity = ?, stock = ?, image_path = ? WHERE id = ?";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(updateQuery)) {
                pstmt.setString(1, name);
                pstmt.setDouble(2, price);
                pstmt.setInt(3, quantity);
                pstmt.setInt(4, stock);
                pstmt.setString(5, selectedImagePath);
                pstmt.setInt(6, id);

                int affectedRows = pstmt.executeUpdate();
                if (affectedRows > 0) {
                    lblError.setText("Product updated successfully.");
                    loadProducts();
                } else {
                    lblError.setText("Failed to update product. Product not found.");
                }
            }
        } catch (NumberFormatException e) {
            lblError.setText("Please enter valid numbers for ID, price, quantity, and stock.");
        } catch (SQLException e) {
            lblError.setText("Database error: " + e.getMessage());
        }
    }

    private void deleteProduct() {
        Product selectedProduct = tableView.getSelectionModel().getSelectedItem();
        if (selectedProduct == null) {
            lblError.setText("Please select a product to delete.");
            return;
        }

        try {
            String deleteQuery = "DELETE FROM products WHERE id = ?";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(deleteQuery)) {
                pstmt.setInt(1, selectedProduct.getId());

                int affectedRows = pstmt.executeUpdate();
                if (affectedRows > 0) {
                    lblError.setText("Product deleted successfully.");
                    loadProducts();
                    clearForm();
                } else {
                    lblError.setText("Failed to delete product.");
                }
            }
        } catch (SQLException e) {
            lblError.setText("Database error: " + e.getMessage());
        }
    }

    private void findProduct() {
        try {
            int id = Integer.parseInt(txtProductId.getText());
            String selectQuery = "SELECT * FROM products WHERE id = ?";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(selectQuery)) {
                pstmt.setInt(1, id);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    Product product = new Product(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getDouble("price"),
                            rs.getInt("quantity"),
                            rs.getInt("stock"),
                            rs.getString("image_path")
                    );
                    tableView.getSelectionModel().select(product);
                    tableView.scrollTo(product);
                    displayProductDetails(product);
                } else {
                    lblError.setText("Product not found.");
                }
            }
        } catch (NumberFormatException e) {
            lblError.setText("Please enter a valid ID.");
        } catch (SQLException e) {
            lblError.setText("Database error: " + e.getMessage());
        }
    }

    private void loadProducts() {
        ObservableList<Product> products = FXCollections.observableArrayList();
        String selectQuery = "SELECT * FROM products";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(selectQuery);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                Product product = new Product(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getDouble("price"),
                        rs.getInt("quantity"),
                        rs.getInt("stock"),
                        rs.getString("image_path")
                );
                products.add(product);
            }
            tableView.setItems(products);
        } catch (SQLException e) {
            lblError.setText("Error loading products: " + e.getMessage());
        }
    }

    private void displayProductDetails(Product product) {
        txtProductId.setText(String.valueOf(product.getId()));
        txtProductName.setText(product.getName());
        txtProductPrice.setText(String.valueOf(product.getPrice()));
        txtProductQuantity.setText(String.valueOf(product.getQuantity()));
        txtProductStock.setText(String.valueOf(product.getStock()));
        selectedImagePath = product.getImagePath();
        if (selectedImagePath != null && !selectedImagePath.isEmpty()) {
            imageView.setImage(new Image(selectedImagePath));
        } else {
            imageView.setImage(null);
        }
    }

    private void clearForm() {
        txtProductId.clear();
        txtProductName.clear();
        txtProductPrice.clear();
        txtProductQuantity.clear();
        txtProductStock.clear();
        imageView.setImage(null);
        selectedImagePath = null;
    }

    public static class Product {
        private int id;
        private String name;
        private double price;
        private int quantity;
        private int stock;
        private String imagePath;

        public Product(int id, String name, double price, int quantity, int stock, String imagePath) {
            this.id = id;
            this.name = name;
            this.price = price;
            this.quantity = quantity;
            this.stock = stock;
            this.imagePath = imagePath;
        }

        public int getId() { return id; }
        public void setId(int id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public double getPrice() { return price; }
        public void setPrice(double price) { this.price = price; }
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
        public int getStock() { return stock; }
        public void setStock(int stock) { this.stock = stock; }
        public String getImagePath() { return imagePath; }
        public void setImagePath(String imagePath) { this.imagePath = imagePath; }
    }
}