package it.polito.mad.team19lab2

import android.content.ClipData
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.TextView
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.ms.square.android.expandabletextview.ExpandableTextView


class ItemDetailsFragment : Fragment() {

    private val longText: String = "È una delle più note e oscure della Commedia, evocata da Virgilio che preannuncia la venuta di questo misterioso personaggio destinato a cacciare e uccidere la lupa-avarizia dall’Italia e dal mondo (il veltro era propriamente un cane usato durante le battute di caccia, dunque perfettamente in grado di mettersi sulle tracce di un animale selvaggio: cfr. Inf., XIII, 126, come veltri ch'uscisser di catena). Su di lui sono state avanzate le più disparate ipotesi, che però, tralasciando le più fantasiose, si riducono a un papa (forse un francescano: il feltro potrebbe alludere al panno del suo saio), a un imperatore (Arrigo VII di Lussemburgo?), a un signore italiano (Cangrande della Scala?). La questione è complicata anche dall'incerta cronologia della composizione di questo Canto, per cui si obietta che se Dante scrisse questi versi intorno al 1307 (è questa l'ipotesi più accreditata, mentre altri pensano addirittura che abbia iniziato la Commedia  prima dell'esilio) era in effetti troppo presto perché potesse pensare ad Arrigo VII, che scese in Italia solo nel 1310-1313, ma anche a Cangrande, che all'epoca aveva appena sedici anni e che il poeta incontrò molto più tardi. Del resto è innegabile che l'elogio a Cangrande messo in bocca all'avo Cacciaguida in Par., XVII, 76 ss. presenti molti punti di contatto con questa profezia e fa propendere per tale identificazione"
    private var item: ItemInfo = ItemInfo()

    companion object {
        fun newInstance() = ItemDetailsFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
        val descriptionTest = view.findViewById<ExpandableTextView>(R.id.expand_text_view)
        descriptionTest.text=longText
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
        b.putString("group19.lab2.TITLE",item.title)
        b.putString("group19.lab2.DESCRIPTION",item.description)
        b.putString("group19.lab2.CATEGORY",item.category)
        b.putString("group19.lab2.LOCATION",item.location)
        b.putFloat("group19.lab2.PRICE",item.price)
        b.putString("group19.lab2.EXPIRY_DATE", item.expiryDate)
    }


}
