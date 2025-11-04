package com.yebur.controller;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

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
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
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
    private String currentBatchId;
    private Boolean isOverviewMode = false;

    private boolean isTransferMode = false;
    private String currentCategoryColor = null;
    private List<Integer> selectedOrderDetailIndexes = new ArrayList<>();

    @FXML
    private VBox orderVboxItems;

    @FXML
    private ScrollPane orderSP;
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
    private NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(Locale.GERMANY);

    @FXML
    public void initialize() {
        javafx.application.Platform.runLater(() -> {
            try {
                this.allCategories = CategoryService.getAllCategories();
            } catch (Exception e) {
                e.printStackTrace();
            }
            reloadCategories();
            orderTotalValue.setText(currencyFormatter.format(BigDecimal.ZERO));
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
                    req.setBatchId(currentBatchId);
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
                this.currentBatchId = UUID.randomUUID().toString();
            } catch (Exception e) {
                e.printStackTrace();
                upsertVisualDetail(product, 1);
                renderDetails(visualDetails, product);
                return;
            }
        }

        try {
            OrderDetailResponse existingVisual = currentdetails.stream()
                    .filter(d -> Objects.equals(d.getProductId(), product.getId()))
                    .findFirst()
                    .orElse(null);

            if (existingVisual != null) {
                existingVisual.setAmount(existingVisual.getAmount() + 1);
            } else {
                OrderDetailResponse newVisual = new OrderDetailResponse();
                newVisual.setProductId(product.getId());
                newVisual.setProductName(product.getName());
                newVisual.setAmount(1);
                newVisual.setUnitPrice(product.getPrice());
                newVisual.setBatchId(currentBatchId);
                currentdetails.add(newVisual);
            }

            OrderDetailRequest createReq = new OrderDetailRequest();
            createReq.setOrderId(currentOrder.getId());
            createReq.setProductId(product.getId());
            createReq.setAmount(1);
            createReq.setUnitPrice(product.getPrice());
            createReq.setStatus("PENDING");
            createReq.setBatchId(currentBatchId);

            OrderDetailService.createOrderDetail(createReq);

            renderDetails(currentdetails, product);

        } catch (Exception e) {
            e.printStackTrace();
            upsertVisualDetail(product, 1);
            renderDetails(visualDetails, product);
        }
    }

    private void openOrder(OrderResponse order) {
        if (order == null)
            return;
        this.currentOrder = order;
        this.currentBatchId = UUID.randomUUID().toString();
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

        BigDecimal total = BigDecimal.ZERO;

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

            BigDecimal unitPrice = d.getUnitPrice();
            Label priceLabel = new Label(currencyFormatter.format(unitPrice));
            priceLabel.setPrefWidth(100);

            BigDecimal totalLine = unitPrice
                    .multiply(BigDecimal.valueOf(d.getAmount()))
                    .setScale(2, RoundingMode.HALF_UP);
            Label totalLabel = new Label(currencyFormatter.format(totalLine));
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
            total = total.add(totalLine);
        }

        orderTotalValue.setText(currencyFormatter.format(total));
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
            d.setBatchId(currentBatchId);
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
        orderTotalValue.setText("0,00€");
        if (hasActiveOrder()) {
            try {
                OrderService.deleteOrder(currentOrder.getId());
                currentOrder = null;
                orderIdLabel.setText("");
                orderTotalValue.setText("0,00€");
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
                orderTotalValue.setText("0,00€");
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
        currentBatchId = UUID.randomUUID().toString();
        for (OrderDetailResponse visualDetail : visualDetails) {
            try {
                OrderDetailRequest createReq = new OrderDetailRequest();
                createReq.setOrderId(order.getId());
                createReq.setProductId(visualDetail.getProductId());
                createReq.setAmount(visualDetail.getAmount());
                createReq.setUnitPrice(visualDetail.getUnitPrice());
                createReq.setStatus("PENDING");
                createReq.setBatchId(currentBatchId);
                System.out.println(currentBatchId);
                System.out.println(createReq.toString());
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
        if (currentOrder == null && visualDetails.isEmpty())
            return;

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
            }

            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setOnHiding(event -> {
                if (controller.anyPaymentDone()) {
                    javafx.application.Platform.runLater(() -> showPaymentBox(controller.getTotalCheck()));
                }
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

    private void showPaymentBox(BigDecimal[] paymentInfo) {
        orderVboxItems.getChildren().clear();

        BigDecimal total = paymentInfo[0].setScale(2, RoundingMode.HALF_UP);
        BigDecimal input = paymentInfo[1].setScale(2, RoundingMode.HALF_UP);
        BigDecimal change = paymentInfo[2].setScale(2, RoundingMode.HALF_UP);

        StackPane wrapper = new StackPane();
        wrapper.setAlignment(Pos.CENTER);
        wrapper.setPrefSize(orderVboxItems.getWidth(), orderVboxItems.getHeight());

        VBox paymentVB = new VBox();
        paymentVB.setAlignment(Pos.CENTER_LEFT);
        paymentVB.setPadding(new javafx.geometry.Insets(15, 15, 15, 15));
        paymentVB.setSpacing(10);
        paymentVB.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 12;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.25), 20, 0, 0, 4);" +
                        "-fx-border-color: #f3f4f6;" +
                        "-fx-border-radius: 12;");

        paymentVB.setMaxWidth(orderVboxItems.getWidth() * 0.9);
        paymentVB.setMaxHeight(orderVboxItems.getHeight() * 0.9);
        paymentVB.setPrefHeight(orderVboxItems.getHeight() * 0.9);
        paymentVB.setPrefWidth(orderVboxItems.getWidth() * 0.9);
        StackPane.setAlignment(paymentVB, Pos.CENTER);
        wrapper.setMargin(paymentVB, new javafx.geometry.Insets(10, 0, 0, 0));

        Label title = new Label("💳 PAGO DE LA CUENTA");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #111827;");

        Pane separator = new Pane();
        separator.setStyle("-fx-border-color: #e5e7eb; -fx-border-width: 0 0 1 0;");
        separator.setPrefHeight(1);

        HBox rowTotal = createPaymentRow("COBRADO:", currencyFormatter.format(total), "#000");
        HBox rowRecibido = createPaymentRow("RECIBIDO:", currencyFormatter.format(input), "#000");
        HBox rowCambio = createPaymentRow("CAMBIO:", currencyFormatter.format(change), "#16a34a");

        if (change.compareTo(BigDecimal.ZERO) > 0) {
            paymentVB.getChildren().addAll(title, separator, rowTotal, rowRecibido, rowCambio);
        } else {
            paymentVB.getChildren().addAll(title, separator, rowTotal);
        }
        wrapper.getChildren().add(paymentVB);

        orderVboxItems.getChildren().add(wrapper);

        paymentVB.setOpacity(0);
        paymentVB.setScaleX(0.9);
        paymentVB.setScaleY(0.9);
        javafx.animation.FadeTransition fade = new javafx.animation.FadeTransition(javafx.util.Duration.millis(300),
                paymentVB);
        fade.setFromValue(0);
        fade.setToValue(1);
        javafx.animation.ScaleTransition scale = new javafx.animation.ScaleTransition(javafx.util.Duration.millis(300),
                paymentVB);
        scale.setFromX(0.9);
        scale.setFromY(0.9);
        scale.setToX(1);
        scale.setToY(1);
        new javafx.animation.ParallelTransition(fade, scale).play();
    }

    private HBox createPaymentRow(String label, String value, String color) {
        HBox row = new HBox(7);
        row.setAlignment(Pos.CENTER_LEFT);

        Label lblText = new Label(label);
        lblText.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #000;");

        Label lblValue = new Label(value);
        lblValue.setStyle("-fx-font-size: 18px; -fx-text-fill: " + color + "; -fx-font-weight: bold;");

        HBox.setHgrow(lblText, javafx.scene.layout.Priority.ALWAYS);
        row.getChildren().addAll(lblText, lblValue);
        return row;
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
