package it.polito.mad.team19lab2.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RatingBar
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentManager
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.ms.square.android.expandabletextview.ExpandableTextView
import it.polito.mad.team19lab2.R
import it.polito.mad.team19lab2.data.ReviewModel
import it.polito.mad.team19lab2.data.UserShortModel

class CommentsRecyclerViewAdapter(var reviewArray: ArrayList<ReviewModel>) : RecyclerView.Adapter<CommentsRecyclerViewAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.fragment_comment, parent, false)
            return ViewHolder(view)
        }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val review = reviewArray[position]
            holder.mIdView.text = review.buyerNick
            holder.mContentView.text = review.comment
            holder.commentratingbar.rating=review.rate
//            with(holder.mView) {
  //              tag = user
    //        }
            //button listener

        }
        override fun getItemCount(): Int = reviewArray.size

        inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {
            val mIdView: TextView = mView.findViewById(R.id.comment_owner_textView)
            val mContentView: ExpandableTextView = mView.findViewById(R.id.expand_comment_text_view)
            val commentratingbar: RatingBar = mView.findViewById(R.id.commentRate)

            override fun toString(): String {
                return super.toString() + " '" + mContentView.text + "'"
            }
        }
}

