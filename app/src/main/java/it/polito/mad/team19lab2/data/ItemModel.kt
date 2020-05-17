package it.polito.mad.team19lab2.data

data class ItemModel (
    var id: String,
    var title: String,
    var description: String,
    var category: String,
    var subcategory: String,
    var imagePath: String,
    var location: String,
    var expiryDate: String,
    var price: Float,
    var sold: Boolean,
    var userId: String
) {
    constructor() : this("","","","","","","","", 0.0F, false, "")
}