package com.codebrew.clikat.modal.other


data class HomeSupplierModel(
        /**
         * status : 200
         * message : Success
         * data : [{"min_order":0,"onOffComm":1,"delivery_prior_time":30,"delivery_min_time":0,"delivery_max_time":0,"urgent_delivery_time":30,"total_reviews":0,"rating":0,"supplier_branch_id":1,"name":"Fashion Planet","logo":"http://45.232.252.46:8082/clikat-buckettest/13347012_1032394443514805_8217217467431659662_n-2mEetEk.png","id":15,"status":0,"start_time":"15:10:00","end_time":"15:10:00","payment_method":0,"commission_package":0},{"min_order":0,"onOffComm":1,"delivery_prior_time":1470,"delivery_min_time":120,"delivery_max_time":1440,"urgent_delivery_time":30,"total_reviews":7,"rating":5,"supplier_branch_id":2,"name":"Metro Shoes Ltd","logo":"http://45.232.252.46:8082/clikat-buckettest/metroaNrdb6.jpg","id":16,"status":0,"start_time":"00:00:00","end_time":"23:55:00","payment_method":0,"commission_package":0},{"min_order":0,"onOffComm":1,"delivery_prior_time":1530,"delivery_min_time":15,"delivery_max_time":15,"urgent_delivery_time":60,"total_reviews":14,"rating":4.2,"supplier_branch_id":3,"name":"Kapsoons","logo":"http://45.232.252.46:8082/clikat-buckettest/150600301972Xq30.jpg","id":17,"status":1,"start_time":"12:46:00","end_time":"12:46:00","payment_method":0,"commission_package":0},{"min_order":0,"onOffComm":1,"delivery_prior_time":1470,"delivery_min_time":420,"delivery_max_time":300,"urgent_delivery_time":30,"total_reviews":0,"rating":0,"supplier_branch_id":4,"name":"Intas Pharmaceuticals Pvt Ltd","logo":"http://45.232.252.46:8082/clikat-buckettest/productN0RbDs.gif","id":18,"status":1,"start_time":"00:00:00","end_time":"23:55:00","payment_method":0,"commission_package":0}]
         */
        var status: Int = 0,
        var message: String? = null,
        var data: List<SupplierDataBean>? = null
)

data class SupplierDataBean(
        var min_order: Int = 0,
        var onOffComm: Int = 0,
        var delivery_prior_time: Int = 0,
        var delivery_min_time: Int = 0,
        var delivery_max_time: Int = 0,
        var urgent_delivery_time: Int = 0,
        var total_reviews: Int = 0,
        var rating: Double = 0.0,
        var supplier_branch_id: Int = 0,
        var name: String? = null,
        var logo: String? = null,
        var id: Int = 0,
        var status: Int = 0,
        var start_time: String? = null,
        var end_time: String? = null,
        var address: String? = null,
        var payment_method: Int = 0,
        var commission_package: Int = 0,
        var viewType: String = "",
        var supplier_image: String? = null,
        var itemModel: HomeItemModel? = null,
        val timing: List<Timing>?=null,
        val category: List<Category>? = null,
        val delivery_radius: Int? = null,
        val description: String? = null,
        val distance: Double? = null,
        val self_pickup: Int? = null,
        val supplier_id:Int?= null,
        val terms_and_conditions: String? = null,
        val uniqueness: String? = null )


data class Timing(
        val end_time: String,
        val is_open: Int,
        val start_time: String,
        val week_id: Int
)

data class Category(
        val category_flow: String,
        val category_id: Int,
        val category_name: String,
        val description: String,
        val image: String,
        val order: Int,
        val supplier_placement_level: Int
)