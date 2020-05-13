package it.polito.mad.team19lab2

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.*
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.ms.square.android.expandabletextview.ExpandableTextView
import kotlinx.android.synthetic.main.item_details_fragment.*
import kotlinx.android.synthetic.main.item_details_fragment.image_view
import kotlinx.android.synthetic.main.item_details_fragment.roundCardView
import org.json.JSONObject
import java.io.File


class ItemDetailsFragment : Fragment() {

    private var item: ItemInfo = ItemInfo()
    private var id_item = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var currentDest = findNavController().currentDestination?.id
        if (currentDest == R.id.nav_on_sale )
            setHasOptionsMenu(false)
        else
            setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.item_details_fragment, container, false)
    }

    override fun onViewCreated (view: View, savedInstanceState : Bundle?){
        super.onViewCreated(view, savedInstanceState)
        val descriptionExpandable = view.findViewById<ExpandableTextView>(R.id.expand_text_view)
        roundCardView.viewTreeObserver.addOnGlobalLayoutListener (object: ViewTreeObserver.OnGlobalLayoutListener{
            override fun onGlobalLayout() {
                roundCardView.radius =  roundCardView.height.toFloat() / 2.0F
                roundCardView.viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        })
        if (arguments != null){
            id_item = arguments?.getString("item_id1").toString()
        }
        val sharedPref = activity?.getSharedPreferences(
            "it.polito.mad.team19lab2.items", Context.MODE_PRIVATE)
        if (sharedPref != null) {
            val currentItem = sharedPref.getString(id_item, "notFound")
            if (currentItem != "notFound") {
                val jo = JSONObject(currentItem)
                item.title = jo.get("TITLE").toString()
                item.description = jo.get("DESCRIPTION").toString()
                item.location = jo.get("LOCATION").toString()
                item.price = jo.get("PRICE").toString().toFloat()
                item.expiryDate = jo.get("DATE").toString()
                item.category = jo.get("CATEGORY").toString()
                item.subCategory=jo.get("SUBCATEGORY").toString()
            }
        }

        val file = File(activity?.applicationContext?.filesDir, "$id_item.png")
        if(file.exists()) {
            item.image = MediaStore.Images.Media.getBitmap(activity?.contentResolver, Uri.fromFile(file))
            image_view.setImageBitmap(item.image)
        }
        titleTextView.text = item.title
        descriptionExpandable.text = item.description
        locationTextView.text = item.location
        priceTextView.text = item.price.toString() + " €"
        expireTextView.text = item.expiryDate
        categoryTextView.text = item.category
        subCategoryTextView.text=item.subCategory
    }

    override fun onCreateOptionsMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.item_details_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == R.id.edit_item_action)
            editItem()
        return super.onOptionsItemSelected(item)
    }

    private fun editItem(){
        findNavController().navigate(R.id.action_nav_item_detail_to_nav_edit_item, bundleOf("item_id1" to id_item))
    }

    //populate and restore bundle do not make the updating visible, only the property are updated
    private fun populateBundle(b:Bundle){
        b.putString("group19.lab2.PATH",item.path)
        b.putString("group19.lab2.TITLE",item.title)
        b.putString("group19.lab2.DESCRIPTION",item.description)
        b.putString("group19.lab2.CATEGORY",item.category)
        b.putString("group19.lab2.LOCATION",item.location)
        b.putFloat("group19.lab2.PRICE",item.price)
        b.putString("group19.lab2.EXPIRY_DATE", item.expiryDate)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        populateBundle(outState)
    }

}
