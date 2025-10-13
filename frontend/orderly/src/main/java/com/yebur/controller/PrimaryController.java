package com.yebur.controller;

import java.util.ArrayList;
import java.util.List;

import com.yebur.model.request.OrderDetailRequest;
import com.yebur.model.request.OrderRequest;
import com.yebur.model.response.CategoryResponse;
import com.yebur.model.response.OrderDetailResponse;
import com.yebur.model.response.OrderResponse;
import com.yebur.model.response.ProductResponse;
import com.yebur.model.response.RestTableResponse;
import com.yebur.model.response.TableWithOrderResponse;
import com.yebur.service.CategoryService;
import com.yebur.service.OrderDetailService;
import com.yebur.service.OrderService;
import com.yebur.service.OverviewService;
import com.yebur.service.ProductService;
import com.yebur.service.RestTableService;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;

public class PrimaryController {

    @FXML
    private TilePane categoryBox;

    @FXML
    private TilePane productBox;

    private List<CategoryResponse> allCategories;
    private List<ProductResponse> allProducts;
    private List<CategoryResponse> categories;
    private List<ProductResponse> productsByCategory;
    private List<OrderResponse> allOrders;
    private List<RestTableResponse> allTables;
    private List<TableWithOrderResponse> overview;
    private List<OrderResponse> orders;
    private OrderResponse currentOrder = null;

    @FXML
    private VBox orderVboxItems;

    @FXML
    private TextField displayField;

    private int currentCategoryPage = 0;
    private int currentProductPage = 0;
    private int currentOrderPage = 0;
    private Long selectedCategoryId;
    private int categoryPageSize;
    private int productPageSize;

    @FXML
    private Label orderIdLabel;
    @FXML
    private Label tableNameLabel;
    @FXML
    private Label orderTotalValue;

    @FXML
    public void initialize() {
        javafx.application.Platform.runLater(() -> {
            try {
                this.allCategories = CategoryService.getAllCategories();
            } catch (Exception e) {
                e.printStackTrace();
            }
            reloadCategories();
        });
    }

    private void reloadCategories() {
        if (categoryPageSize <= 0) {
            categoryPageSize = getMaximumCategories();
        }
        loadCategories(categoryPageSize);
    }

    private void reloadProducts(String color) {
        if (productPageSize <= 0) {
            productPageSize = getMaximumProducts();
        }
        if (selectedCategoryId == null)
            return;
        loadProductsForCategory(productPageSize, selectedCategoryId, color);
    }

    @FXML
    private void handleChecksClick() {
        if (productPageSize <= 0) {
            productPageSize = getMaximumProducts();
        }
        loadOrders(productPageSize);
    }

    private void loadCategories(int slots) {
        categoryBox.getChildren().clear();

        if (allCategories == null || allCategories.isEmpty()) {
            return;
        }

        final int S = Math.max(1, slots);
        final int N = allCategories.size();
        int start = 0;
        int remaining = N;
        for (int p = 0; p < currentCategoryPage && remaining > 0; p++) {
            int capPrev;
            if (p == 0) {
                capPrev = Math.min(S - 1, remaining);
            } else {
                capPrev = Math.min(Math.max(S - 2, 1), remaining);
            }
            start += capPrev;
            remaining -= capPrev;
        }

        if (start >= N && currentCategoryPage > 0) {
            currentCategoryPage--;
            reloadCategories();
            return;
        }

        boolean hasPrev = currentCategoryPage > 0;
        int navSlots = hasPrev ? 1 : 0;

        int capIfNoNext = Math.max(S - navSlots, 1);
        int capIfNext = Math.max(capIfNoNext - 1, 1);

        boolean hasNext;
        int capacity;

        if (remaining > capIfNoNext) {
            hasNext = true;
            capacity = capIfNext;
        } else {
            hasNext = false;
            capacity = Math.min(remaining, capIfNoNext);
        }

        int end = Math.min(start + capacity, N);
        this.categories = allCategories.subList(start, end);

        for (CategoryResponse category : this.categories) {
            Button btn = new Button(category.getName());
            btn.getStyleClass().add("category-btn");
            btn.setStyle("-fx-background-color: " +
                    (category.getColor() != null ? category.getColor() : "#f9fafb"));
            btn.setOnAction(e -> {
                selectedCategoryId = category.getId();
                currentProductPage = 0;
                reloadProducts(category.getColor());
            });
            categoryBox.getChildren().add(btn);
        }
        if (hasPrev) {
            Button prevBtn = new Button("←");
            prevBtn.getStyleClass().add("category-btn");
            prevBtn.setOnAction(e -> {
                currentCategoryPage--;
                loadCategories(S);
            });
            categoryBox.getChildren().add(0, prevBtn);
        }

        if (hasNext) {
            Button nextBtn = new Button("→");
            nextBtn.getStyleClass().add("category-btn");
            nextBtn.setOnAction(e -> {
                currentCategoryPage++;
                loadCategories(S);
            });
            categoryBox.getChildren().add(nextBtn);
        }
    }

    private void loadProductsForCategory(int slots, Long selectedCategoryId, String color) {
        productBox.getChildren().clear();

        try {
            this.allProducts = ProductService.getProductsByCategory(selectedCategoryId);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        if (allProducts == null || allProducts.isEmpty()) {
            return;
        }

        final int S = Math.max(1, slots);
        final int N = allProducts.size();

        int start = 0;
        int remaining = N;
        for (int p = 0; p < currentProductPage && remaining > 0; p++) {
            int capPrev;
            if (p == 0) {
                capPrev = Math.min(S - 1, remaining);
            } else {
                capPrev = Math.min(Math.max(S - 2, 1), remaining);
            }
            start += capPrev;
            remaining -= capPrev;
        }

        if (start >= N && currentProductPage > 0) {
            currentProductPage--;
            loadProductsForCategory(S, selectedCategoryId, color);
            return;
        }

        boolean hasPrev = currentProductPage > 0;
        int navSlots = hasPrev ? 1 : 0;

        int capIfNoNext = Math.max(S - navSlots, 1);
        int capIfNext = Math.max(capIfNoNext - 1, 1);

        boolean hasNext;
        int capacity;

        if (remaining > capIfNoNext) {
            hasNext = true;
            capacity = capIfNext;
        } else {
            hasNext = false;
            capacity = Math.min(remaining, capIfNoNext);
        }

        int end = Math.min(start + capacity, N);
        this.productsByCategory = allProducts.subList(start, end);

        if (hasPrev) {
            Button prevBtn = new Button("←");
            prevBtn.getStyleClass().add("product-btn");
            prevBtn.setOnAction(e -> {
                currentProductPage--;
                loadProductsForCategory(S, selectedCategoryId, color);
            });
            productBox.getChildren().add(prevBtn);
        }

        for (ProductResponse product : this.productsByCategory) {
            Button btn = new Button(product.getName());
            btn.getStyleClass().add("product-btn");
            btn.setStyle("-fx-background-color: " + (color != null ? color : "#f9fafb"));
            btn.setOnAction(e -> {
                createOrderIfNotExists();
                addProductToOrder(product);
                updateOrderTotal();
            });
            productBox.getChildren().add(btn);
        }
        if (hasNext) {
            if (!productBox.getChildren().isEmpty()) {
                productBox.getChildren().remove(productBox.getChildren().size() - 1);
            }

            Button nextBtn = new Button("→");
            nextBtn.getStyleClass().add("product-btn");
            nextBtn.setOnAction(e -> {
                currentProductPage++;
                loadProductsForCategory(S, selectedCategoryId, color);
            });
            productBox.getChildren().add(nextBtn);
        }
    }

    private int getMaximumCategories() {
        int cols = categoryBox.getPrefColumns() > 0 ? categoryBox.getPrefColumns() : 3;
        double h = categoryBox.getParent().getLayoutBounds().getHeight();
        double tileH = 75;
        double vgap = categoryBox.getVgap();

        if (h <= 0) {
            return cols * 3;
        }

        int rows = (int) Math.floor((h + vgap) / (tileH + vgap));
        rows = Math.max(rows, 1);

        return cols * rows;
    }

    private int getMaximumProducts() {
        int cols = productBox.getPrefColumns() > 0 ? productBox.getPrefColumns() : 5;

        double h = productBox.getLayoutBounds().getHeight();

        double tileH;
        if (!productBox.getChildren().isEmpty()) {
            tileH = productBox.getChildren().get(0).getLayoutBounds().getHeight();
        } else {
            tileH = 60;
        }

        double vgap = productBox.getVgap();

        if (h <= 0) {
            return cols * 3;
        }

        int rows = (int) Math.floor((h + vgap * 0.9) / (tileH + vgap));
        rows = Math.max(rows, 1);

        return cols * rows;
    }

    private void numberFieldClicked() {

    }

    private void loadOrders(int slots) {
        productBox.getChildren().clear();

        try {
            this.overview = OverviewService.getOverview();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        if (overview == null || overview.isEmpty()) {
            System.out.println("No tables or orders to display");
            return;
        }

        final int S = Math.max(1, slots);
        final int N = overview.size();

        int start = 0;
        int remaining = N;
        for (int p = 0; p < currentOrderPage && remaining > 0; p++) {
            int capPrev = (p == 0) ? Math.min(S - 1, remaining) : Math.min(Math.max(S - 2, 1), remaining);
            start += capPrev;
            remaining -= capPrev;
        }

        if (start >= N && currentOrderPage > 0) {
            currentOrderPage--;
            loadOrders(S);
            return;
        }

        boolean hasPrev = currentOrderPage > 0;
        int navSlots = hasPrev ? 1 : 0;

        int capIfNoNext = Math.max(S - navSlots, 1);
        int capIfNext = Math.max(capIfNoNext - 1, 1);

        boolean hasNext;
        int capacity;

        if (remaining > capIfNoNext) {
            hasNext = true;
            capacity = capIfNext;
        } else {
            hasNext = false;
            capacity = Math.min(remaining, capIfNoNext);
        }

        int end = Math.min(start + capacity, N);
        List<TableWithOrderResponse> currentOverview = overview.subList(start, end);

        if (hasPrev) {
            Button prevBtn = new Button("←");
            prevBtn.getStyleClass().add("product-btn");
            prevBtn.setOnAction(e -> {
                currentOrderPage--;
                loadOrders(S);
            });
            productBox.getChildren().add(prevBtn);
        }

        for (TableWithOrderResponse item : currentOverview) {
            String buttonName;

            if (item.getTableId() == null) {
                buttonName = "Cuenta #" + item.getOrder().getOrderId() + "\n$" + item.getOrder().getTotal();
            } else {
                buttonName = item.getTableName() + "\n$" + item.getOrder().getTotal();
            }

            Button btn = new Button(buttonName);
            btn.getStyleClass().add("product-btn");
            btn.setStyle("-fx-background-color: #f9fafb");
            btn.setOnAction(e -> {
                currentOrder = null;
                tableNameLabel.setText("");
                if (item.getOrder().getOrderId() == null) {
                    orderIdLabel.setText("");
                    tableNameLabel.setText(item.getTableName());
                } else {
                    try {
                        openOrder(OrderService.getOrderById(item.getOrder().getOrderId()));
                    } catch (Exception ex) {
                        System.out.println("Error opening order: " + ex.getMessage());
                    }
                }
            });
            productBox.getChildren().add(btn);
        }

        if (hasNext) {
            Button nextBtn = new Button("→");
            nextBtn.getStyleClass().add("product-btn");
            nextBtn.setOnAction(e -> {
                currentOrderPage++;
                loadOrders(S);
            });
            productBox.getChildren().add(nextBtn);
        }
    }

    private void openOrder(OrderResponse order) {
        orderVboxItems.getChildren().clear(); // очищаем старые позиции
        List<OrderDetailResponse> details = new ArrayList<>();

        try {
            List<OrderDetailResponse> response = OrderDetailService.getOrderDetailsByOrderId(order.getId());

            if (response == null || response.isEmpty()) {
                System.out.println("No order details found for order ID: " + order.getId());
            } else {
                details = response;
            }

        } catch (Exception e) {
            System.out.println("Error getting order details in openOrder(): " + e.getMessage());
            e.printStackTrace();
        }

        if (!details.isEmpty()) {
            // Добавляем каждую позицию
            for (OrderDetailResponse detail : details) {
                HBox row = new HBox(10);
                row.getStyleClass().add("order-item-row");

                // Количество
                Label quantityLabel = new Label("x" + detail.getAmount());
                quantityLabel.getStyleClass().add("quantity-label");
                quantityLabel.setPrefWidth(40);
                quantityLabel.setAlignment(Pos.CENTER);

                // Название товара
                Label nameLabel = new Label("Producto #" + detail.getProductId());
                nameLabel.setPrefWidth(340);
                nameLabel.setWrapText(true);

                // Цена за единицу
                Label priceLabel = new Label(String.format("$%.2f", detail.getUnitPrice()));
                priceLabel.getStyleClass().add("price-label");
                priceLabel.setPrefWidth(100);

                // Общая сумма
                double total = detail.getUnitPrice() * detail.getAmount();
                Label totalLabel = new Label(String.format("$%.2f", total));
                totalLabel.getStyleClass().add("total-label");
                totalLabel.setPrefWidth(100);

                row.getChildren().addAll(quantityLabel, nameLabel, priceLabel, totalLabel);
                orderVboxItems.getChildren().add(row);
            }
        }

        // Заголовок заказа
        orderIdLabel.setText("Cuenta #" + order.getId());
        tableNameLabel.setText(order.getRestTable() != null ? order.getRestTable().getName().toString() : "");

        // Пересчитать общую сумму
        double totalOrder = 0;
        for (OrderDetailResponse detail : details) {
            totalOrder += detail.getUnitPrice() * detail.getAmount();
        }
        orderTotalValue.setText(String.format("$%.2f", totalOrder));
    }

    private void createOrderIfNotExists() {
        if (currentOrder == null) {
            try {
                // Здесь можно предусмотреть выбор клиента и стола, но пока — дефолтные
                OrderResponse newOrder = OrderService.createOrder(new OrderRequest("OPEN")); // null = клиент не выбран
                this.currentOrder = newOrder;
                orderIdLabel.setText("Cuenta #" + newOrder.getId());
                tableNameLabel.setText(
                        newOrder.getRestTable().getName() != null ? newOrder.getRestTable().getName().toString()
                                : "null");
                orderVboxItems.getChildren().clear();
                orderTotalValue.setText("$0.00");

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void addProductToOrder(ProductResponse product) {
        if (!orderIdLabel.equals("")) {
            createOrderIfNotExists();
        }
        int amount = 1;
        // Проверка на наличие продукта
        for (Node node : orderVboxItems.getChildren()) {
            if (node instanceof HBox row) {
                Label nameLabel = (Label) row.getChildren().get(1);
                if (nameLabel.getText().equals(product.getName())) {
                    Label quantityLabel = (Label) row.getChildren().get(0);
                    Label totalPriceLabel = (Label) row.getChildren().get(3);

                    int currentAmount = Integer.parseInt(quantityLabel.getText().replace("x", ""));
                    currentAmount++;
                    quantityLabel.setText("x" + currentAmount);

                    double totalProductPrice = product.getPrice() * currentAmount;
                    totalPriceLabel.setText(String.format("$%.2f", totalProductPrice));
                    return;
                }
            }
        }

        // Создание новой строки
        HBox row = new HBox(10);
        row.getStyleClass().add("order-item-row");

        Label quantityLabel = new Label("x" + amount);
        quantityLabel.getStyleClass().add("quantity-label");
        quantityLabel.setPrefWidth(40);
        quantityLabel.setAlignment(Pos.CENTER);

        Label nameLabel = new Label(product.getName());
        nameLabel.setPrefWidth(340);
        nameLabel.setWrapText(true);

        Label priceLabel = new Label(String.format("$%.2f", product.getPrice()));
        priceLabel.getStyleClass().add("price-label");
        priceLabel.setPrefWidth(100);

        Label totalPriceLabel = new Label(String.format("$%.2f", product.getPrice() * amount));
        totalPriceLabel.getStyleClass().add("total-label");
        totalPriceLabel.setPrefWidth(100);

        row.getChildren().addAll(quantityLabel, nameLabel, priceLabel, totalPriceLabel);
        orderVboxItems.getChildren().add(row);
        if (orderIdLabel.equals("")) {
            try {
                OrderDetailRequest detail = new OrderDetailRequest();
                detail.setOrderId(currentOrder.getId());
                System.out.println(detail.getOrderId());
                detail.setProductId(product.getId());
                detail.setAmount(amount);
                detail.setUnitPrice(product.getPrice());

                OrderDetailService.createOrderDetail(detail);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void updateOrderTotal() {
        double total = 0;
        for (Node node : orderVboxItems.getChildren()) {
            if (node instanceof HBox row) {
                Label totalLabel = (Label) row.getChildren().get(3);
                total += Double.parseDouble(totalLabel.getText().replace("$", "").replace(",", "."));
            }
        }
        orderTotalValue.setText(String.format("$%.2f", total));
    }

}
