package com.yebur.controller;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.yebur.app.App;
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
import com.yebur.ui.CustomDialog;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class PrimaryController {

    @FXML
    private TilePane categoryBox;
    @FXML
    private TilePane productBox;

    private List<CategoryResponse> allCategories;
    private List<ProductResponse> allProducts;
    private List<CategoryResponse> categories;
    private List<ProductResponse> productsByCategory;
    private List<TableWithOrderResponse> overview;

    private List<OrderDetailResponse> currentdetails = new ArrayList<>();
    private OrderResponse currentOrder = null;
    private RestTableResponse selectedTable = null;
    private final List<OrderDetailResponse> visualDetails = new ArrayList<>();
    private TableWithOrderResponse currentOverviewItem = null;
    private Boolean isOverviewMode = false;

    private boolean isTransferMode = false;
    private String currentCategoryColor = null;
    private List<Integer> selectedOrderDetailIndexes = new ArrayList<>();

    @FXML
    private VBox orderVboxItems;
    @FXML
    private TextField displayField;
    @FXML
    private Label orderIdLabel;
    @FXML
    private Label tableNameLabel;
    @FXML
    private Label orderTotalValue;

    private int currentCategoryPage = 0;
    private int currentProductPage = 0;
    private int currentOrderPage = 0;
    private Long selectedCategoryId;
    private int categoryPageSize;
    private int productPageSize;

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
                currentCategoryColor = category.getColor();
                reloadProducts(currentCategoryColor);
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

    private void reloadProducts(String color) {
        if (productPageSize <= 0) {
            productPageSize = getMaximumProducts();
        }
        if (selectedCategoryId == null)
            return;
        loadProductsForCategory(productPageSize, selectedCategoryId, color);
    }

    private void loadProductsForCategory(int slots, Long selectedCategoryId, String color) {
        productBox.getChildren().clear();
        isOverviewMode = false;

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
            btn.setOnAction(e -> onProductClick(product));
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

    @FXML
    private void handleChecksClick() {
        currentOrder = null;
        visualDetails.clear();
        orderVboxItems.getChildren().clear();
        currentdetails.clear();
        orderIdLabel.setText("");
        tableNameLabel.setText("");
        orderTotalValue.setText("$0,00");
        selectedTable = null;
        if (productPageSize <= 0)
            productPageSize = getMaximumProducts();
        loadOrders(productPageSize);
    }

    private void loadOrders(int slots) {
        isOverviewMode = true;
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
            Label nameLabel;
            Label totalLabel;

            if (item.getTableId() == null) {
                nameLabel = new Label("Cuenta #" + item.getOrder().getOrderId());
            } else {
                nameLabel = new Label(item.getTableName());
            }

            totalLabel = new Label("$" + item.getOrder().getTotal());

            nameLabel.getStyleClass().add("tablewithorder-name-label");
            totalLabel.getStyleClass().add("tablewithorder-total-label");

            VBox buttonNameVB = new VBox(1, nameLabel, totalLabel);
            buttonNameVB.setAlignment(Pos.CENTER);

            Button btn = new Button();
            btn.setGraphic(buttonNameVB);
            btn.getStyleClass().add("product-btn");
            btn.setStyle("-fx-background-color: #f9fafb;");
            btn.setOnAction(e -> {
                onOverviewItemClick(item);
                this.currentOverviewItem = item;
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

    private void onOverviewItemClick(TableWithOrderResponse item) {
        if (isTransferMode) {
            Long targetOrderId;
            try {
                if (item.getOrder() != null && item.getOrder().getOrderId() != null) {
                    targetOrderId = item.getOrder().getOrderId();
                } else {
                    OrderRequest newOrderReq = new OrderRequest("OPEN", item.getTableId());
                    OrderResponse newOrder = OrderService.createOrder(newOrderReq);
                    targetOrderId = newOrder.getId();
                }

                for (OrderDetailResponse visualDetail : visualDetails) {
                    OrderDetailRequest req = new OrderDetailRequest();
                    req.setOrderId(targetOrderId);
                    req.setProductId(visualDetail.getProductId());
                    req.setAmount(visualDetail.getAmount());
                    req.setUnitPrice(visualDetail.getUnitPrice());
                    req.setStatus("PENDING");
                    OrderDetailService.createOrderDetail(req);
                }

                exitTransferMode();

            } catch (Exception e) {
                e.printStackTrace();
            }

            return;
        }

        visualDetails.clear();
        currentOrder = null;

        if (item.getTableId() != null) {
            selectedTable = new RestTableResponse();
            selectedTable.setId(item.getTableId());
            selectedTable.setName(item.getTableName());
            tableNameLabel.setText(item.getTableName());
        } else {
            selectedTable = null;
        }

        if (item.getOrder() == null || item.getOrder().getOrderId() == null) {
            orderIdLabel.setText("");
            renderDetails(visualDetails, null);
            return;
        }

        try {
            OrderResponse order = OrderService.getOrderById(item.getOrder().getOrderId());
            openOrder(order);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void onProductClick(ProductResponse product) {
        if (!hasActiveOrder() && selectedTable == null) {
            upsertVisualDetail(product, 1);
            renderDetails(visualDetails, product);
            return;
        }

        if (!hasActiveOrder() && selectedTable != null) {
            try {
                OrderResponse newOrder = OrderService.createOrder(new OrderRequest("OPEN", selectedTable.getId()));
                currentOrder = newOrder;
                orderIdLabel.setText("Cuenta #" + newOrder.getId());
                tableNameLabel.setText(selectedTable.getName());
            } catch (Exception e) {
                e.printStackTrace();
                upsertVisualDetail(product, 1);
                renderDetails(visualDetails, product);
                return;
            }
        }

        try {
            List<OrderDetailResponse> existingDetails = OrderDetailService
                    .getUnpaidOrderDetailsByOrderId(currentOrder.getId());

            OrderDetailResponse existingDetail = existingDetails.stream()
                    .filter(d -> Objects.equals(d.getProductId(), product.getId()))
                    .findFirst()
                    .orElse(null);

            if (existingDetail != null) {
                int newAmount = existingDetail.getAmount() + 1;
                existingDetail.setAmount(newAmount);

                OrderDetailRequest updateReq = new OrderDetailRequest();
                updateReq.setOrderId(currentOrder.getId());
                updateReq.setProductId(product.getId());
                updateReq.setAmount(newAmount);
                updateReq.setUnitPrice(product.getPrice());
                try {
                    OrderDetailService.updateOrderDetail(existingDetail.getId(), updateReq);
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            } else {
                OrderDetailRequest createReq = new OrderDetailRequest();
                createReq.setOrderId(currentOrder.getId());
                createReq.setProductId(product.getId());
                createReq.setAmount(1);
                createReq.setUnitPrice(product.getPrice());
                createReq.setStatus("PENDING");
                OrderDetailService.createOrderDetail(createReq);
            }

            currentdetails = OrderDetailService.getUnpaidOrderDetailsByOrderId(currentOrder.getId());
            renderDetails(currentdetails, product);
        } catch (Exception e) {
            upsertVisualDetail(product, 1);
            renderDetails(visualDetails, product);
        }

    }

    private void openOrder(OrderResponse order) {
        if (order == null)
            return;
        this.currentOrder = order;
        this.visualDetails.clear();

        orderIdLabel.setText("Cuenta #" + order.getId());
        if (order.getRestTable() != null) {
            selectedTable = order.getRestTable();
            tableNameLabel.setText(order.getRestTable().getName());
        } else {
            selectedTable = null;
            tableNameLabel.setText("");
        }

        try {
            currentdetails = OrderDetailService.getUnpaidOrderDetailsByOrderId(order.getId());
            renderDetails(currentdetails != null ? currentdetails : new ArrayList<>(), null);
        } catch (Exception e) {
            e.printStackTrace();
            renderDetails(new ArrayList<>(), null);
        }
    }

    private void renderDetails(List<OrderDetailResponse> details, ProductResponse product) {
        orderVboxItems.getChildren().clear();
        double total = 0;

        for (OrderDetailResponse d : details) {
            HBox row = new HBox(10);
            row.getStyleClass().add("order-item-row");

            Label qty = new Label("x" + d.getAmount());
            qty.setPrefWidth(40);
            qty.setAlignment(Pos.CENTER);

            String name = d.getProductName() != null ? d.getProductName()
                    : (product != null ? product.getName() : "Producto #" + d.getProductId());
            Label nameLabel = new Label(name);
            nameLabel.setPrefWidth(340);
            nameLabel.setWrapText(true);

            Label priceLabel = new Label(String.format("$%.2f", d.getUnitPrice()));
            priceLabel.setPrefWidth(100);

            double totalLine = d.getUnitPrice() * d.getAmount();
            Label totalLabel = new Label(String.format("$%.2f", totalLine));
            totalLabel.setPrefWidth(100);

            row.getChildren().addAll(qty, nameLabel, priceLabel, totalLabel);

            row.setOnMouseClicked(event -> {
                boolean isSelected = row.getStyleClass().contains("selected-row-order-item");

                if (!isSelected) {
                    row.getStyleClass().add("selected-row-order-item");
                    selectedOrderDetailIndexes.add(details.indexOf(d));
                } else {
                    row.getStyleClass().remove("selected-row-order-item");
                    selectedOrderDetailIndexes.remove(Integer.valueOf(details.indexOf(d)));
                }
            });

            orderVboxItems.getChildren().add(row);
            total += totalLine;
        }

        orderTotalValue.setText(String.format("$%.2f", total));

    }

    private void upsertVisualDetail(ProductResponse product, int delta) {
        OrderDetailResponse exist = visualDetails.stream()
                .filter(d -> Objects.equals(d.getProductId(), product.getId()))
                .findFirst().orElse(null);

        if (exist == null && delta > 0) {
            OrderDetailResponse d = new OrderDetailResponse();
            d.setProductId(product.getId());
            d.setProductName(product.getName());
            d.setUnitPrice(product.getPrice());
            d.setAmount(delta);
            visualDetails.add(d);
        } else if (exist != null) {
            int newAmt = Math.max(0, exist.getAmount() + delta);
            exist.setAmount(newAmt);
            if (newAmt == 0)
                visualDetails.remove(exist);
        }
    }

    @FXML
    private void handleClearOrderClick() {
        if (orderVboxItems.getChildren().isEmpty() && !hasActiveOrder()) {
            return;
        }
        orderVboxItems.getChildren().clear();
        visualDetails.clear();
        currentdetails.clear();
        orderTotalValue.setText("0.00");
        if (hasActiveOrder()) {
            try {
                OrderService.deleteOrder(currentOrder.getId());
                currentOrder = null;
                orderIdLabel.setText("");
                orderTotalValue.setText("$0,00");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (isOverviewMode) {
            loadOrders(productPageSize);
        }
    }

    @FXML
    private void handleDeleteOrderItemClick() {
        if (orderVboxItems.getChildren().isEmpty()) {
            return;
        }

        if (selectedOrderDetailIndexes == null || selectedOrderDetailIndexes.isEmpty()) {
            removeLastDetail();
        } else {
            removeSelectedDetails();
        }

        if ((currentdetails == null || currentdetails.isEmpty()) && hasActiveOrder()) {
            try {
                OrderService.deleteOrder(currentOrder.getId());
                currentOrder = null;
                orderIdLabel.setText("");
                tableNameLabel.setText("");
                orderTotalValue.setText("$0.00");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (isOverviewMode) {
            loadOrders(productPageSize);
        }
    }

    private void removeLastDetail() {
        if ((currentdetails == null || currentdetails.isEmpty()) && !visualDetails.isEmpty()) {
            visualDetails.remove(visualDetails.size() - 1);
            renderDetails(visualDetails, null);
            return;
        }

        if (currentdetails != null && !currentdetails.isEmpty()) {
            OrderDetailResponse lastDetail = currentdetails.get(currentdetails.size() - 1);
            currentdetails.remove(lastDetail);

            try {
                OrderDetailService.deleteOrderDetail(lastDetail.getId());
            } catch (Exception e) {
                e.printStackTrace();
            }
            renderDetails(currentdetails, null);
        }
    }

    private void removeSelectedDetails() {
        if (selectedOrderDetailIndexes == null || selectedOrderDetailIndexes.isEmpty()) {
            return;
        }

        selectedOrderDetailIndexes.sort((a, b) -> b - a);

        for (Integer index : selectedOrderDetailIndexes) {
            if (index < 0 || index >= currentdetails.size()) {
                continue;
            }

            OrderDetailResponse detailToRemove = currentdetails.get(index);
            currentdetails.remove(detailToRemove);

            try {
                OrderDetailService.deleteOrderDetail(detailToRemove.getId());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        selectedOrderDetailIndexes.clear();

        for (Node node : orderVboxItems.getChildren()) {
            node.getStyleClass().remove("selected-row");
        }

        renderDetails(currentdetails, null);
    }

    @FXML
    private void handleNewOrderClick() {
        if (hasActiveOrder()) {
            return;
        }

        if (visualDetails.isEmpty()) {
            return;
        }

        Stage stage = (Stage) categoryBox.getScene().getWindow();
        int result = CustomDialog.show(stage,
                "Confirmación",
                "¿Desea crear un nuevo pedido o traspasar los productos a otra mesa/pedido?",
                "Nuevo",
                "Traspasar",
                "Cancelar");

        switch (result) {
            case 1 -> {
                try {

                    OrderRequest orderReq = new OrderRequest("OPEN", null);
                    OrderResponse newOrder = OrderService.createOrder(orderReq);

                    if (!visualDetails.isEmpty()) {
                        attachVisualDetailsToOrder(newOrder);
                    }
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            }

            case 0 -> {
                enterTransferMode();
            }

            case -1 -> {
                return;
            }

            default -> System.err.println("⚠️ Resultado desconocido del diálogo");
        }
    }

    private void enterTransferMode() {
        isTransferMode = true;

        for (Node n : categoryBox.getChildren()) {
            n.setDisable(true);
        }

        loadOrders(productPageSize);
    }

    private void exitTransferMode() {
        isTransferMode = false;

        for (Node n : categoryBox.getChildren()) {
            n.setDisable(false);
        }
        visualDetails.clear();
        renderDetails(new ArrayList<>(), null);

        currentOrderPage = 0;

        javafx.application.Platform.runLater(() -> {
            try {
                loadOrders(productPageSize);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void attachVisualDetailsToOrder(OrderResponse order) {
        for (OrderDetailResponse visualDetail : visualDetails) {
            try {
                OrderDetailRequest createReq = new OrderDetailRequest();
                createReq.setOrderId(order.getId());
                createReq.setProductId(visualDetail.getProductId());
                createReq.setAmount(visualDetail.getAmount());
                createReq.setUnitPrice(visualDetail.getUnitPrice());
                createReq.setStatus("PENDING");
                OrderDetailService.createOrderDetail(createReq);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        visualDetails.clear();
        renderDetails(new ArrayList<>(), null);
    }

    @FXML
    private void handlePartialPaymentClick() {
        if(currentOrder == null){
            if(visualDetails.isEmpty()) {
                return;
            }
        }
        try {
            URL fxml = getClass().getResource("/com/yebur/payment.fxml");
            if (fxml == null) {
                System.err.println("FXML not found: /com/yebur/payment.fxml");
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxml);
            Parent root = loader.load();

            PartialPaymentController controller = loader.getController();
            controller.setPrimaryController(this);

            Stage stage = new Stage();
            stage.setTitle("Dividir cuenta / Pago parcial");
            Scene scene = new Scene(root);
            stage.initStyle(StageStyle.UNDECORATED);
            stage.setResizable(false);
            stage.setScene(scene);

            URL cssUrl = App.class.getResource("/com/yebur/styles/partialpayment.css");
            if (cssUrl != null) {
                scene.getStylesheets().clear();
                scene.getStylesheets().add(cssUrl.toExternalForm());
            } else {
                System.err.println("CSS not found: /com/yebur/styles/partialpayment.css");
            }

            stage.initModality(Modality.APPLICATION_MODAL);

            stage.setOnHiding(event -> {
                handleChecksClick();
            });

            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean hasActiveOrder() {
        return currentOrder != null && currentOrder.getId() != null;
    }

    private int getMaximumCategories() {
        int cols = categoryBox.getPrefColumns() > 0 ? categoryBox.getPrefColumns() : 3;
        double h = categoryBox.getParent().getLayoutBounds().getHeight();
        double tileH = 75;
        double vgap = categoryBox.getVgap();
        if (h <= 0)
            return cols * 3;
        int rows = (int) Math.floor((h + vgap) / (tileH + vgap));
        rows = Math.max(rows, 1);
        return cols * rows;
    }

    private int getMaximumProducts() {
        int cols = productBox.getPrefColumns() > 0 ? productBox.getPrefColumns() : 5;
        double h = productBox.getLayoutBounds().getHeight();
        double vgap = productBox.getVgap();

        double tileH = 0;
        if (!productBox.getChildren().isEmpty()) {
            tileH = productBox.getChildren().get(0).getLayoutBounds().getHeight();
        }
        if (tileH <= 0)
            tileH = 58;

        if (h <= 0)
            return cols * 3;

        double availableRows = (h + vgap) / (tileH + vgap);

        int rows = (int) Math.floor(availableRows + 0.5);

        rows = Math.max(rows, 1);

        return cols * rows - 1;
    }

    @FXML
    private void handleCloseClick() {
        Stage stage = (Stage) categoryBox.getScene().getWindow();
        stage.close();
    }

    public List<OrderDetailResponse> getCurrentdetails() {
        return currentdetails;
    }

    public OrderResponse getCurrentOrder() {
        return currentOrder;
    }

    public RestTableResponse getSelectedTable() {
        return selectedTable;
    }

    public List<OrderDetailResponse> getVisualDetails() {
        return visualDetails;
    }
}
