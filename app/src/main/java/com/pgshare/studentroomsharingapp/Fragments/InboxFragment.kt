package com.pgshare.studentroomsharingapp.Fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.pgshare.studentroomsharingapp.Adapter.InboxAdapter
import com.pgshare.studentroomsharingapp.Adapter.RecentChat
import com.pgshare.studentroomsharingapp.databinding.FragmentInboxBinding

class InboxFragment : Fragment() {

    private var _binding: FragmentInboxBinding? = null
    private val binding get() = _binding!!

    private lateinit var inboxAdapter: InboxAdapter
    private val inboxList = ArrayList<RecentChat>()

    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInboxBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()

        if (currentUserId.isNotEmpty()) {
            fetchInboxMessages()
        } else {
            // User is not logged in
            binding.progressBarInbox.visibility = View.GONE
            binding.layoutEmptyState.visibility = View.VISIBLE
        }
    }

    private fun setupRecyclerView() {
        binding.recyclerViewInbox.layoutManager = LinearLayoutManager(requireContext())
        inboxAdapter = InboxAdapter(inboxList)
        binding.recyclerViewInbox.adapter = inboxAdapter
    }

    private fun fetchInboxMessages() {
        binding.progressBarInbox.visibility = View.VISIBLE
        binding.recyclerViewInbox.visibility = View.GONE
        binding.layoutEmptyState.visibility = View.GONE

        // Point directly to the current user's personal inbox node
        val inboxRef = FirebaseDatabase.getInstance().getReference("inbox").child(currentUserId)

        inboxRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Safety check to prevent crashes if fragment is hidden
                if (!isAdded || _binding == null) return

                inboxList.clear()

                for (dataSnapshot in snapshot.children) {
                    val chatPreview = dataSnapshot.getValue(RecentChat::class.java)
                    if (chatPreview != null) {
                        inboxList.add(chatPreview)
                    }
                }

                // Sort the list so the most recent messages are at the very top
                inboxList.sortByDescending { it.timestamp }

                binding.progressBarInbox.visibility = View.GONE

                if (inboxList.isEmpty()) {
                    binding.recyclerViewInbox.visibility = View.GONE
                    binding.layoutEmptyState.visibility = View.VISIBLE
                } else {
                    binding.layoutEmptyState.visibility = View.GONE
                    binding.recyclerViewInbox.visibility = View.VISIBLE
                    inboxAdapter.notifyDataSetChanged()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                if (isAdded && _binding != null) {
                    binding.progressBarInbox.visibility = View.GONE
                    Toast.makeText(requireContext(), "Failed to load inbox", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}