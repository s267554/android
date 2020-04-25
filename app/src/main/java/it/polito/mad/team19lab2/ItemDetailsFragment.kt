package it.polito.mad.team19lab2

import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.*
import android.widget.AutoCompleteTextView
import android.widget.EditText
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.ms.square.android.expandabletextview.ExpandableTextView
import kotlinx.android.synthetic.main.fragment_edit_item.*
import kotlinx.android.synthetic.main.item_details_fragment.*
import kotlinx.android.synthetic.main.item_details_fragment.image_view
import kotlinx.android.synthetic.main.item_details_fragment.roundCardView
import org.json.JSONObject
import java.io.File


class ItemDetailsFragment : Fragment() {

    //private val longText: String = "È una delle più note e oscure della Commedia, evocata da Virgilio che preannuncia la venuta di questo misterioso personaggio destinato a cacciare e uccidere la lupa-avarizia dall’Italia e dal mondo (il veltro era propriamente un cane usato durante le battute di caccia, dunque perfettamente in grado di mettersi sulle tracce di un animale selvaggio: cfr. Inf., XIII, 126, come veltri ch'uscisser di catena). Su di lui sono state avanzate le più disparate ipotesi, che però, tralasciando le più fantasiose, si riducono a un papa (forse un francescano: il feltro potrebbe alludere al panno del suo saio), a un imperatore (Arrigo VII di Lussemburgo?), a un signore italiano (Cangrande della Scala?). La questione è complicata anche dall'incerta cronologia della composizione di questo Canto, per cui si obietta che se Dante scrisse questi versi intorno al 1307 (è questa l'ipotesi più accreditata, mentre altri pensano addirittura che abbia iniziato la Commedia  prima dell'esilio) era in effetti troppo presto perché potesse pensare ad Arrigo VII, che scese in Italia solo nel 1310-1313, ma anche a Cangrande, che all'epoca aveva appena sedici anni e che il poeta incontrò molto più tardi. Del resto è innegabile che l'elogio a Cangrande messo in bocca all'avo Cacciaguida in Par., XVII, 76 ss. presenti molti punti di contatto con questa profezia e fa propendere per tale identificazione"
    private var item: ItemInfo = ItemInfo()
    private var id_item = ""

    companion object {
        fun newInstance() = ItemDetailsFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
//        if(savedInstanceState!=null){
//            restoreFromBundle(savedInstanceState)
//        }
        // Restore from Shared Prefs
        id_item = "item_id"
        val sharedPref = activity?.getSharedPreferences(
            "it.polito.mad.team19lab2"+id_item, 0)
        if (sharedPref != null) {
            val currentItem = sharedPref.getString("item", "notFound")
            if (currentItem != "notFound") {
                val jo = JSONObject(currentItem)
                item.title = jo.get("TITLE").toString()
                item.description = jo.get("DESCRIPTION").toString()
                item.location = jo.get("LOCATION").toString()
                item.price = jo.get("PRICE").toString().toFloat()
                item.expiryDate = jo.get("DATE").toString()
                item.category = jo.get("CATEGORY").toString()
//                item.path = jo.get("PATH").toString()
            }
        }
        Log.d("xxx", "Oncreate Completed")
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
                roundCardView.viewTreeObserver.removeOnGlobalLayoutListener(this);
            }
        })

        val file = File(activity?.applicationContext?.filesDir, "$id_item.png")
        if(file.exists()) {
            item.image = MediaStore.Images.Media.getBitmap(activity?.contentResolver, Uri.fromFile(file))
            image_view.setImageBitmap(item.image)
        }
        titleTextView.text = item.title
        descriptionExpandable.text = item.description
        locationTextView.text = item.location
        priceTextView.text = item.price.toString()
        expireTextView.text = item.expiryDate
        categoryTextView.text = item.category
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
        val b=Bundle()
        populateBundle(b)
        val navController = findNavController()
        navController.navigate(R.id.action_nav_item_detail_to_nav_edit_item, b)
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

    private fun restoreFromBundle(b:Bundle){
        item.path=b.getString("group19.lab2.PATH", "")
        item.title=b.getString("group19.lab2.TITLE", "")
        item.description=b.getString("group19.lab2.DESCRIPTION", "")
        item.location = b.getString("group19.lab2.LOCATION", "")
        item.price=b.getFloat("group19.lab2.PRICE", 0F)
        item.expiryDate=b.getString("group19.lab2.EXPIRY_DATE", "")
        item.category=b.getString("group19.lab2.CATEGORY", "")
    }



}
