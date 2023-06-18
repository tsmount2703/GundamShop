/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controllers;

import db.Cart;
import db.Discount;
import db.DiscountFacade;
import db.Item;
import db.Order;
import db.OrderDetail;
import db.OrderFacade;
import db.Product;
import db.ProductFacade;
import db.User;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 *
 * @author acer
 */
@WebServlet(name = "ProductController", urlPatterns = {"/product"})
public class ProductController extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        String controller = (String) request.getAttribute("controller");
        String action = (String) request.getAttribute("action");
        ProductFacade pf = new ProductFacade();
        HttpSession session = request.getSession();
        switch (action) {
            case "product_details":
                try {
                    //Đọc mẫu tin cần sửa vào đối tượng toy
                    String product_id = request.getParameter("productId");
                    Product product = pf.read(product_id);
                    //Lưu toy vào request để truyền cho view edit.jsp
                    request.setAttribute("product", product);
                    //Chuyển request & response đến view edit.jsp để xử lý tiếp
                    request.getRequestDispatcher("/layouts/main.jsp").forward(request, response);
                } catch (SQLException ex) {
                    //Hien trang thong bao loi
                    ex.printStackTrace();//in thong bao loi chi tiet cho developer
                    request.setAttribute("message", ex.getMessage());
                    request.setAttribute("controller", "error");
                    request.setAttribute("action", "error");
                    request.getRequestDispatcher("/layouts/main.jsp").forward(request, response);
                }
                break;
            case "product_summary":
                //Processing code here
                //Forward request & response to the main layout
                try {
                    String op = request.getParameter("op");
                    switch (op) {
                        case "list": {
                            pf = new ProductFacade();
                            request.setAttribute("pf", pf);
                            //Quay ve cart page
                            request.getRequestDispatcher("/layouts/main.jsp").forward(request, response);
                        }
                        break;
                        case "add": {
                            String product_id = request.getParameter("product_id");
                            Product product = pf.read(product_id);
                            Item item = new Item(product, 1);
                            //Lấy giỏ hàng từ session ra
                            Cart cart = (Cart) session.getAttribute("cart");
                            if (cart == null) {
                                //Nếu chưa có giỏ hàng thì tạo giỏ hàng mới
                                cart = new Cart();
                                session.setAttribute("cart", cart);
                            }
                            // Add item vào giỏ hàng
                            cart.add(item);
                            //request.getRequestDispatcher("/product/products.do?op=productAll").forward(request, response);
                            request.getRequestDispatcher("/product/products.do?op=productAll").forward(request, response);
                        }
                        break;
                        case "increase": {
                            int quantity = Integer.parseInt(request.getParameter("quantity"));
                            String product_id = request.getParameter("product_id");
                            Product product = pf.read(product_id);
                            Item item = new Item(product, quantity + 1);//Moi lan add 1 san pham
                            //Lay gio hang tu session
                            Cart cart = (Cart) session.getAttribute("cart");
                            //Add item into cart
                            cart.update(product_id, quantity + 1);
                            //request.getRequestDispatcher("/product/products.do?op=productAll").forward(request, response);
                            request.getRequestDispatcher("/layouts/main.jsp").forward(request, response);
                        }
                        break;
                        case "decrease": {
                            String product_id = request.getParameter("product_id");
                            int quantity = Integer.parseInt(request.getParameter("quantity"));
                            if (quantity > 1) {
                                Product product = pf.read(product_id);
                                Item item = new Item(product, quantity - 1);//Moi lan add 1 san pham
                                //Lay gio hang tu session
                                Cart cart = (Cart) session.getAttribute("cart");
                                //Add item into cart
                                cart.update(product_id, quantity - 1);
                            }
                            //request.getRequestDispatcher("/product/products.do?op=productAll").forward(request, response);
                            request.getRequestDispatcher("/layouts/main.jsp").forward(request, response);
                        }
                        break;
                        case "delete": {
                            String product_id = request.getParameter("product_id");
                            //Lấy giỏ hàng từ session ra
                            Cart cart = (Cart) session.getAttribute("cart");
                            // Remove item vào giỏ hàng
                            cart.remove(product_id);
                            request.getRequestDispatcher("/layouts/main.jsp").forward(request, response);
                        }
                        break;
                        case "empty": {
                            Cart cart = (Cart) session.getAttribute("cart");
                            // Remove item vào giỏ hàng
                            cart.empty();
                            session.setAttribute("discount", null);
                            request.getRequestDispatcher("/layouts/main.jsp").forward(request, response);
                            break;
                        }
                        case "update": {
                            String product_id = request.getParameter("product_id");
                            int quantity = Integer.parseInt(request.getParameter("quantity"));
                            if (quantity > 0) {
                                //Lấy giỏ hàng từ session ra
                                Cart cart = (Cart) session.getAttribute("cart");
                                // Update quantity
                                cart.update(product_id, quantity);
                            }
                            request.getRequestDispatcher("/layouts/main.jsp").forward(request, response);
                        }
                        break;
                        case "discount":
                            try {
                                User user = (User) session.getAttribute("User");
                                //Đọc mẫu tin cần sửa vào đối tượng toy
                                if (user == null) {
                                    request.setAttribute("messaged", "Voucher chỉ có hiẹu lực khi bạn đăng nhập!");
                                } else {
                                    String discountId = request.getParameter("discountId");
                                    DiscountFacade df = new DiscountFacade();
                                    Discount discount = df.readVoucher(discountId);
                                    //Lưu toy vào request để truyền cho view edit.jsp
                                    if (discount == null) {
                                        request.setAttribute("messaged", "Mã giảm giá không tồn tại!");

                                    } else {
                                        session.setAttribute("discount", discount);
                                    }
                                }
                                request.getRequestDispatcher("/layouts/main.jsp").forward(request, response);
                                //Chuyển request & response đến view edit.jsp để xử lý tiếp
                            } catch (SQLException ex) {
                                //Hien trang thong bao loi
                                ex.printStackTrace();//in thong bao loi chi tiet cho developer
                                request.setAttribute("message", ex.getMessage());
                                request.getRequestDispatcher("/layouts/main.jsp").forward(request, response);
                            }
                            break;
                    }
                } catch (Exception ex) {
                    //Hien trang thong bao loi
                    ex.printStackTrace();//In thông báo chi tiết cho developer
                    request.setAttribute("message", ex.getMessage());
                    request.getRequestDispatcher("/layouts/main.jsp").forward(request, response);
                }
                break;
            case "billing":
                try {
                    String op = request.getParameter("op");
                    switch (op) {
                        case "print": {
                            User user = (User) session.getAttribute("User");
                            if (user == null) {
                                request.getRequestDispatcher("/user/login.do").forward(request, response);
                            } else {
                                request.getRequestDispatcher("/layouts/main.jsp").forward(request, response);
                            }
                            break;
                        }

                        case "checkout": {
                            Cart cart = (Cart) session.getAttribute("cart");
                            User user = (User) session.getAttribute("User");
                            String order_note = request.getParameter("order_note");
                            String paymentMethod = request.getParameter("paymentMethod");
                            Discount discount = (Discount) session.getAttribute("discount");
                            if (user == null) {
                                request.getRequestDispatcher("/user/login.do").forward(request, response);
                            } else {
                                OrderFacade of = new OrderFacade();
                                int discountRate = 0;
                                if (discount != null) {
                                    discountRate = discount.getDiscountRate();
                                }
                                int order_id = of.addOrder(user, cart, order_note, paymentMethod, discountRate);
                                cart.empty();
                                Order order = of.read(order_id);
                                List<OrderDetail> odList;
                                odList = of.readAllProducts(order_id);
                                session.setAttribute("discount", null);
                                request.setAttribute("orderId", order_id);
                                request.setAttribute("order", order);
                                request.setAttribute("odList", odList);
                                request.setAttribute("message", "Đơn hàng của bạn đã được xử lý!");
                                request.getRequestDispatcher("/product/transaction_success.do").forward(request, response);
                            }

                        }
                        break;
                        case "cancel": {
                            response.sendRedirect(request.getContextPath() + "/product/product_summary.do?op=list");
                            break;
                        }

                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    request.setAttribute("message", ex.getMessage());
                    request.getRequestDispatcher("/layouts/main.jsp").forward(request, response);
                }
                break;

            //!!Trang thong bao mua hang thanh cong
            case "transaction_success":
                request.getRequestDispatcher("/layouts/main.jsp").forward(request, response);
                break;

            case "products"://Hien thi product by TYPEs
                try {
                    List<Product> list = (List<Product>) session.getAttribute("list");
                    List<Product> sortedList, sortedSubList;
                    int page = 1;
                    int recordsPerPage = 6;
                    if (request.getParameter("page") != null) {
                        page = Integer.parseInt(request.getParameter("page"));
                    }
                    int startIndex = (page - 1) * recordsPerPage + 1;
                    int endIndex = recordsPerPage * page;
                    String op = request.getParameter("op");
                    String subOp;
                    String oldOp = (String) session.getAttribute("op");
                    session.getAttribute("subOp");
                    switch (op) {
                        case "productAll":
                            list = pf.select(startIndex, endIndex);
                            request.setAttribute("list", list);
                            request.setAttribute("type_id", "Mọi thể loại");
                            request.setAttribute("total", pf.getTotal());
                            session.setAttribute("total", pf.getTotal());
                            session.setAttribute("type_id", "Mọi thể loại");
                            break;
                        case "productByType":
                            String type_id = request.getParameter("type_id");
                            list = pf.getAllProductsByTypeId(type_id, startIndex, endIndex);
                            request.setAttribute("list", list);
                            request.setAttribute("type_id", type_id);
                            int totalByType_id = pf.totalByTypeID(type_id);
                            request.setAttribute("total", totalByType_id);
                            session.setAttribute("total", totalByType_id);
                            session.setAttribute("type_id", type_id);
                            break;
                        case "sort":
                            subOp = request.getParameter("subOp");
                            String oldType_id = (String) session.getAttribute("type_id");
                            String oldNamePart = (String) session.getAttribute("namePart");
                            if (oldOp.equals("productAll")) {
                                list = pf.select();
                            }
                            if (oldOp.equals("productByType")) {
                                list = pf.getAllProductsByTypeId(oldType_id);
                            }
                            if (oldOp.equals("search")) {
                                list = pf.search(oldNamePart);
                            }
                            switch (subOp) {
                                case "ascPrice":
                                    sortedList = pf.sortByPriceAsc(list);
                                    if (endIndex < list.size()) {
                                        sortedSubList = sortedList.subList(startIndex - 1, endIndex);
                                    } else {
                                        sortedSubList = sortedList.subList(startIndex - 1, list.size());
                                    }
                                    request.setAttribute("list", sortedSubList);
                                    request.setAttribute("subOpName", "Giá tăng dần");
                                    break;
                                case "descPrice":
                                    sortedList = pf.sortByPriceDesc(list);
                                    if (endIndex < list.size()) {
                                        sortedSubList = sortedList.subList(startIndex - 1, endIndex);
                                    } else {
                                        sortedSubList = sortedList.subList(startIndex - 1, list.size());
                                    }
                                    request.setAttribute("list", sortedSubList);
                                    request.setAttribute("subOpName", "Giá giảm dần");

                                    break;
                                default:
                                    sortedList = list;
                                    if (endIndex < list.size()) {
                                        sortedSubList = sortedList.subList(startIndex - 1, endIndex);
                                    } else {
                                        sortedSubList = sortedList.subList(startIndex - 1, list.size());
                                    }
                                    request.setAttribute("list", sortedSubList);
                                    request.setAttribute("subOpName", "Mặc định");
                                    break;
                            }
                            request.setAttribute("subOp", subOp);
                            session.setAttribute("subOp", subOp);
                            break;

                        case "search":
                            String namePart = request.getParameter("productName");
                            list = pf.search(namePart, startIndex, endIndex);
                            request.setAttribute("list", list);
                            int totalByName = pf.totalByName(namePart);
                            request.setAttribute("total", totalByName);
                            session.setAttribute("type_id", "Theo tên");
                            session.setAttribute("total", totalByName);
                            session.setAttribute("namePart", namePart);
                            break;
                    }
                    request.setAttribute("currentPage", page);
                    session.setAttribute("list", list);
                    session.setAttribute("op", op);
                    session.setAttribute("currentPage", page);

                    request.getRequestDispatcher("/layouts/main.jsp").forward(request, response);
                } catch (SQLException ex) {
                    //Hien trang thong bao loi
                    ex.printStackTrace();//In thông báo chi tiết cho developer
                    request.setAttribute("message", ex.getMessage());
                    request.getRequestDispatcher("/layouts/main.jsp").forward(request, response);
                }
                break;
            default:
            //Show error page
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
