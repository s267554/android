import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import it.polito.mad.team19lab2.R
import it.polito.mad.team19lab2.data.UserShortModel
import java.util.ArrayList

class InterestedUsersRecycleViewAdapter(
    private val mValues: ArrayList<UserShortModel>
) : RecyclerView.Adapter<InterestedUsersRecycleViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_user, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = mValues[position] as UserShortModel
        holder.mIdView.text = user.nickname
        holder.mContentView.text = user.fullname

        with(holder.mView) {
            tag = user
        }

        holder.mViewButton.setOnClickListener {
            it.findNavController().navigate(
                R.id.action_nav_item_detail_to_nav_show_profile,
                bundleOf("user_id" to user.id)
            )
        }
    }

    override fun getItemCount(): Int = mValues.size

    inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {
        val mIdView: TextView = mView.findViewById(R.id.user_fullname)
        val mContentView: TextView = mView.findViewById(R.id.user_nickname)
        val mViewButton: Button = mView.findViewById(R.id.user_list_view_button)

        override fun toString(): String {
            return super.toString() + " '" + mContentView.text + "'"
        }
    }
}