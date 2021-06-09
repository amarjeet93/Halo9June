package com.codebrew.clikat.data

enum class AppDataType(val type: Int) {
    Ecom(2),
    Food(1),
    HomeServ(8),
    CarRental(5),
    Beauty(6),
    Custom(10)
}

enum class FoodAppType(val foodType: Int) {
    Pickup(2),
    Delivery(0),
    Both(2)
}

enum class DeliveryType(val type: String) {
    PickupOrder("PICKUP"),
    DeliveryOrder("DELIVERY")
}

enum class SearchType(val type: String) {
    TYPE_PROD("product"),
    TYPE_RESTU("restaurant")
}

enum class VendorAppType(val appType: Int) {
    Single(1),
    Multiple(0)
}

enum class PaymentType(val payType: Int) {
    CASH(0),
    ONLINE(1),
    BOTH(2)
}


enum class OrderStatus(val orderStatus: Double) {
    Pending(0.0),
    Approved(1.0),
    Confirmed(1.0),
    Rejected(2.0),
    Packed(2.5),
    In_Kitchen(2.5),
    On_The_Way(3.0),
    Started(3.0),
    Near_You(4.0),
    Ended(5.0),
    Delivered(5.0),
    Rating_Given(6.0),
    Track(7.0),
    Customer_Canceled(8.0),
    Scheduled(9.0),
    Ready_to_be_picked(2.6),
    Reached(2.6),
    Shipped(2.6)
}

enum class ReturnStatus(val returnStatus: Int) {
    Return_requested(0),
    Agent_on_the_way(1),
    Product_picked(2),
    returned(3)
}

enum class RequestsStatus(val status: Double) {
    Pending(0.0),
    Approved(1.0),
    AdminRejected(2.0),
    UserCancelled(3.0),
    Cancelled(4.0)
}

enum class SearchDataType(val searchType: Int) {
    Product(0),
    Supplier(1),
    Both(2)
}

enum class OrderPayment(val payment: Int) {
    ReceiptOrder(1),
    EditOrder(2),
    PaymentAfterConfirm(4)
}
