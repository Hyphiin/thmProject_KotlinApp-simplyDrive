package de.thm.mow.felixwegener.simplydrive.fragments

import android.Manifest
import android.app.AlertDialog
import android.content.ContentValues
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.CodeScannerView
import com.budiyev.android.codescanner.DecodeCallback
import com.budiyev.android.codescanner.ErrorCallback
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import de.thm.mow.felixwegener.simplydrive.R
import de.thm.mow.felixwegener.simplydrive.Route
import java.util.*


class ScanFragment : Fragment() {

    private lateinit var codeScanner: CodeScanner

    //private lateinit var scanView: View
    //private lateinit var scanText: TextView

    private lateinit var contextF: FragmentActivity
    private var startDrive: Boolean = true

    private lateinit var driveId: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        //scanView = inflater.inflate(R.layout.fragment_scan, container, false)
        //scanText = scanView.findViewById(R.id.tv__Scan)

        val activity = requireActivity()
        contextF = activity
        setupPermission()

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_scan, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val scannerView = view.findViewById<CodeScannerView>(R.id.scanner_view)
        val activity = requireActivity()
        codeScanner = CodeScanner(activity, scannerView)
        codeScanner.decodeCallback = DecodeCallback {
            activity.runOnUiThread {
                //scanText.text = it.text
                createDialog(it.text)
                //Toast.makeText(activity, it.text, Toast.LENGTH_LONG).show()
            }
        }
        codeScanner.errorCallback = ErrorCallback { // or ErrorCallback.SUPPRESS
            activity.runOnUiThread {
                Toast.makeText(
                    activity, "Camera initialization error: ${it.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
        scannerView.setOnClickListener {
            codeScanner.startPreview()
        }
    }

    override fun onResume() {
        super.onResume()
        codeScanner.startPreview()
    }

    override fun onPause() {
        codeScanner.releaseResources()
        super.onPause()
    }

    private fun setupPermission() {
        val permission =
            ContextCompat.checkSelfPermission(contextF, android.Manifest.permission.CAMERA)

        if (permission != PackageManager.PERMISSION_GRANTED) {
            makeRequest()
        }
    }

    private fun makeRequest() {
        ActivityCompat.requestPermissions(contextF, arrayOf(Manifest.permission.CAMERA), 101)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            101 -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(
                        contextF,
                        "You need the camera permission to be granted!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun createDialog(text: String) {
        val builder = AlertDialog.Builder(contextF)

        val delimiter = ";"

        val list = text.split(delimiter)

        Log.d(".......XXX", list.toString())

        startDrive = if (startDrive) {
            builder.setMessage("${list[0]} mit ${list[1]}")
                .setPositiveButton("Fahrt starten!") { _, _ -> addHistory(list) }
                .setNegativeButton("Abbrechen") { dialogInterface, _ -> dialogInterface.dismiss() }

            false
        } else {
            builder.setMessage("Aussteigen in ${list[0]}")
                .setPositiveButton("Fahrt beenden!") { _, _ -> editHistory(list) }
                .setNegativeButton("Abbrechen") { dialogInterface, _ -> dialogInterface.dismiss() }

            true
        }


        val dialog = builder.create()
        dialog.show()
    }

    private fun addHistory(list: List<String>) {

        // anzupassen, wenn nur noch 2 Parameter drinne stehen
        if (list.size === 2) {
            val user = FirebaseAuth.getInstance().currentUser
            user?.let {
                val uid = user.uid

                val db = Firebase.firestore

                val start = list[0]
                val line = list[1]

                val c = Calendar.getInstance()

                val year = c.get(Calendar.YEAR)
                val month = c.get(Calendar.MONTH)
                val day = c.get(Calendar.DAY_OF_MONTH)

                val date = "$day-$month-$year"

                val hour = c.get(Calendar.HOUR_OF_DAY)
                val minute = c.get(Calendar.MINUTE)
                val sec = c.get(Calendar.SECOND)

                val time = "$hour:$minute:$sec"

                val route = Route(date, time, start, "", line, uid)

                Log.d("ROUTE_____________X", route.toString())

                db.collection("routes")
                    .add(route)
                    .addOnSuccessListener { documentReference ->
                        Log.d(
                            ContentValues.TAG,
                            "DocumentSnapshot added with ID: ${documentReference.id}"
                        )
                        driveId = documentReference.id

                    }
                    .addOnFailureListener { e ->
                        Log.w(ContentValues.TAG, "Error adding document", e)

                    }
            }
        }

    }

    private fun editHistory(list: List<String>) {
        val user = FirebaseAuth.getInstance().currentUser
        user?.let {
            val db = Firebase.firestore

            db.collection("routes")
                .document(driveId).update("end", list[0])
        }
    }

}