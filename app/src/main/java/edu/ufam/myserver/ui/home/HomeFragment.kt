package edu.ufam.myserver.ui.home

import android.R
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import edu.ufam.myserver.databinding.FragmentHomeBinding
import kotlin.collections.ArrayList

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    private val binding get() = _binding!!
    private val db = FirebaseFirestore.getInstance()
    private val emailUser = Firebase.auth.currentUser?.email.toString()
    private var serverList: MutableList<String> = ArrayList()
    private var serverSelected = 0
    private val historicoTemperatura = mutableListOf<Entry>()
    private var refCollectionServers = db.collection(emailUser).document("DataServer")
        .collection("Servers")

    lateinit var lineChart: LineChart

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val spnServer: Spinner = binding.spnServer

        refCollectionServers.get().addOnSuccessListener { result ->
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
                refCollectionServers.document(document)
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
                binding.txtTemperature.text = buildString {
                    append("0.0")
                    append("°")
                }
                updateGraphic()
            }
        }

        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Este método é chamado antes que o texto seja alterado
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Este método é chamado quando o texto é alterado
                // Aqui você pode chamar a função que deseja executar
            }

            override fun afterTextChanged(s: Editable?) {
                // Este método é chamado depois que o texto é alterado
                updateGraphic()
            }
        }

        binding.txtTemperature.addTextChangedListener(textWatcher)

        return root
    }

    private fun lineGraphic(historicoTemperatura: MutableList<Entry>) {
        lineChart = binding.lineGraphic
        val list: ArrayList<Entry> = historicoTemperatura as ArrayList<Entry>

        val lineDataSet = LineDataSet(list, "List")

        lineDataSet.setColors(ColorTemplate.MATERIAL_COLORS, 255)
        lineDataSet.valueTextColor = Color.BLACK

        val lineData = LineData(lineDataSet)

        lineChart.data = lineData

        lineChart.description.text = "Temperatura"

        lineChart.performLongClick()
//        lineChart.animateY(10)
        lineChart.performClick()
    }

    private fun updateGraphic() {
        val query = refCollectionServers.document(serverList[serverSelected])
            .collection("History").orderBy("date")
        query.get().addOnSuccessListener { documentos ->
            historicoTemperatura.clear()
            var cont = 0
            for (documento in documentos) {
                val temp = documento.getDouble("temp")
                if (temp != null) {
                    historicoTemperatura.add(Entry(cont.toFloat(), temp.toFloat()))
                }
                cont += 10
            }
            lineGraphic(historicoTemperatura)
        }
    }

    private fun updateTemperature(name: String, temperature: String) {
        if (_binding != null) {
            if (serverList[serverSelected] == name) {
                binding.txtTemperature.text = buildString {
                    append(temperature)
                    append("°")
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        serverList.clear()
    }
}