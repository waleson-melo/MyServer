package edu.ufam.myserver.ui.home

import android.R
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.fragment.app.Fragment
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import edu.ufam.myserver.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val emailUser = Firebase.auth.currentUser?.email
    private val db = FirebaseFirestore.getInstance()

    private var serverList: MutableList<String> = ArrayList()
    private var serverSelected = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val spnServer: Spinner = binding.spnServer

        // Pegando os Servidores cadastrados e colocando dentro do Spinner
        db.collection(emailUser!!).document("DataServer")
            .collection("Servers").get().addOnSuccessListener { result ->
                for (document in result) {
                    serverList.add(document.id)
                }
                spnServer.adapter = ArrayAdapter(
                    requireActivity().applicationContext,
                    R.layout.simple_spinner_dropdown_item,
                    serverList
                )

                // Aplicando a cada servidor um evento de atualização da temperatura
                for (document in serverList) {
                    db.collection(emailUser).document("DataServer")
                        .collection("Servers").document(document)
                        .addSnapshotListener { value, error ->
                            if (value != null) {
                                val name = value.id
                                val temperature = value.getDouble("currentTemperature").toString()
                                updateTemperature(name, temperature)
                            }
                        }
                }
            }

        spnServer.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                serverSelected = position
            }
        }

        return root
    }

    // Altera o texto da temperatura de acordo com o item selecionado no Spienner
    private fun updateTemperature(name: String, temperature: String) {
        if (serverList[serverSelected] == name) {
            binding.txtTemperature.text = buildString {
                append(temperature)
                append("°")
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        serverSelected = 0
        serverList.clear()
    }
}