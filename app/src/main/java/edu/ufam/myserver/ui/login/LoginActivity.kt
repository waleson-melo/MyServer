package edu.ufam.myserver.ui.login

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.*
import com.google.firebase.firestore.FirebaseFirestore
import edu.ufam.myserver.MainActivity
import edu.ufam.myserver.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnLogin.setOnClickListener{ view ->
            val email = binding.inpEmail.text.toString()
            val password = binding.inpPassword.text.toString()

            if (!emptyInput(view, email, password)) {
                auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { authentication ->
                    if (authentication.isSuccessful) {
                        navigateToMainActivity()
                    }
                }.addOnFailureListener { exception ->
                    val messageError = when (exception) {
                        is FirebaseAuthEmailException -> "Digite um email válido!"
                        is FirebaseAuthInvalidCredentialsException -> "Login ou senha inválida!"
                        is FirebaseNetworkException -> "Sem conexão com a internet!"
                        else -> "Erro ao entrar!"
                    }
                    snackBarMessage(view, Color.RED, messageError)
                }
            }
        }

        binding.btnRegister.setOnClickListener { view ->
            val email = binding.inpEmail.text.toString()
            val password = binding.inpPassword.text.toString()

            if (!emptyInput(view, email, password)) {
                auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { create ->
                    if (create.isSuccessful) {
                        snackBarMessage(view, Color.BLUE , "Sucesso ao registrar usuário.")

                        val initServerMap = hashMapOf(
                            "name" to "Server",
                            "current_temperature" to 0.00
                        )
                        FirebaseFirestore.getInstance().collection(email).document("Server")
                            .set(initServerMap)
                            .addOnCompleteListener {
                                Log.d("db", "Sucesso ao salvar dados do servidor")
                            }
                    }
                }.addOnFailureListener { exception ->
                    val messageError = when (exception) {
                        is FirebaseAuthWeakPasswordException -> "Digite uma senha com no mínimo 6 caracteres!"
                        is FirebaseAuthInvalidCredentialsException -> "Digite um email válido!"
                        is FirebaseAuthUserCollisionException -> "Esta conta já possui registro!"
                        is FirebaseNetworkException -> "Sem conexão com a internet!"
                        else -> "Erro ao cadastrar usuário!"
                    }
                    snackBarMessage(view, Color.RED, messageError)
                }
            }
        }
    }

    private fun snackBarMessage(view: View, color: Int, message: String) {
        val snackBar = Snackbar.make(view, message, Snackbar.LENGTH_SHORT)
        snackBar.setBackgroundTint(color)
        snackBar.show()
    }

    private fun emptyInput(view: View, email: String, password: String) : Boolean {
        if (email.isEmpty() or password.isEmpty()) {
            snackBarMessage(view, Color.RED, "Preencha todos os campos!")
            return true
        }
        return false
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}