package thuy.ptithcm.spotifyclone.ui.auth


import android.app.DatePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import thuy.ptithcm.spotifyclone.R
import thuy.ptithcm.spotifyclone.data.Status
import thuy.ptithcm.spotifyclone.databinding.FragmentSignupBinding
import thuy.ptithcm.spotifyclone.di.Injection
import thuy.ptithcm.spotifyclone.utils.*
import thuy.ptithcm.spotifyclone.viewmodel.AuthViewModel
import java.util.*

class SignupFragment : Fragment(), TextWatcher {

    private lateinit var binding: FragmentSignupBinding
    private lateinit var viewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders
            .of(this, Injection.provideAuthViewModelFactory())
            .get(AuthViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSignupBinding.inflate(inflater, container, false)
        binding.viewmodel = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViewModel()
        addEvents()
    }

    private fun addEvents() {
        binding.btnBackSignUp.setOnClickListener { requireActivity().onBackPressed() }
        binding.btnSignUp.setOnClickListener {
            it.hideKeyboard()
            signUpAcc()
        }
        // Edit text change listener
        binding.edtEmail.addTextChangedListener(this)
        binding.edtUserName.addTextChangedListener(this)
        binding.edtDayOfBirth.addTextChangedListener(this)
        binding.edtDayOfBirth.setOnClickListener { showDatetimePicker(binding.edtDayOfBirth) }
        binding.edtPassword.addTextChangedListener(this)
        binding.edtConfirmPassword.addTextChangedListener(this)
    }

    private fun showDatetimePicker(editText: EditText) {
        var cal = Calendar.getInstance()
        var yyyy = cal.get(Calendar.YEAR)
        var mm = cal.get(Calendar.MONTH)
        var dd = cal.get(Calendar.DAY_OF_MONTH)

        if (editText.getTextTrim() != "") {
            val dateArr = editText.getTextTrim().split("/")
            yyyy = dateArr[2].toInt()
            mm = dateArr[1].toInt() - 1
            dd = dateArr[0].toInt()
        } else {
            yyyy -= 14
        }

        val dpd = DatePickerDialog(
            requireContext(),
            DatePickerDialog.OnDateSetListener { _, yearDl, monthOfYear, dayOfMonth ->
                var date1 = dayOfMonth.toString()
                var month1 = (monthOfYear + 1).toString()
                if (date1.length == 1) date1 = "0$date1"
                if (month1.length == 1) month1 = "0$month1"
                val date = "$date1/$month1/$yearDl"
                editText.setText(date)
            }, yyyy, mm, dd
        )
        cal = Calendar.getInstance()
        // Set min date
        cal[Calendar.MONTH] = cal.get(Calendar.MONTH)
        cal[Calendar.DAY_OF_MONTH] = cal.get(Calendar.DAY_OF_MONTH)
        cal[Calendar.YEAR] = cal.get(Calendar.YEAR) - 100

        cal = Calendar.getInstance()
        // Set max date
        cal[Calendar.MONTH] = cal.get(Calendar.MONTH)
        cal[Calendar.DAY_OF_MONTH] = cal.get(Calendar.DAY_OF_MONTH)
        cal[Calendar.YEAR] = cal.get(Calendar.YEAR) - 14
        dpd.datePicker.maxDate = cal.timeInMillis
        dpd.show()
    }

    private fun signUpAcc() {
        var checkTextOk = true
        if (!isValidEmail(binding.edtEmail.getTextTrim())) {
            binding.edtEmail.error = "Invalid email!"
            checkTextOk = false
        }
        if (!isValidPassword(binding.edtPassword.getTextTrim())) {
            binding.edtPassword.error = "Password must be at least 6 characters!"
            checkTextOk = false
        }
        if (binding.edtConfirmPassword.getTextTrim() != binding.edtPassword.getTextTrim()) {
            binding.edtConfirmPassword.error = "Confirm password does not match!"
            checkTextOk = false
        }
        if (checkTextOk) {
            viewModel.signUp(
                binding.edtEmail.getTextTrim(),
                binding.edtUserName.getTextTrim(),
                binding.edtDayOfBirth.getTextTrim(),
                binding.radGender.let {
                    binding.radMale.isChecked
                },
                binding.edtPassword.getTextTrim()
            )
        }
    }

    private fun bindViewModel() {
        viewModel.resultData.observe(this, Observer {
            when (it?.status) {
                Status.FAILED -> {
                    binding.progressbarSignUp.invisible()
                    Toast.makeText(requireContext(), it.msg, Toast.LENGTH_LONG).show()
                    binding.btnSignUp.isEnabled = true
                    binding.btnSignUp.backgroundTintList =
                        ContextCompat.getColorStateList(requireContext(), R.color.colorOrange)
                }
                Status.RUNNING -> {
                    binding.progressbarSignUp.visible()
                    binding.btnSignUp.isEnabled = false
                    binding.btnSignUp.backgroundTintList =
                        ContextCompat.getColorStateList(
                            requireContext(),
                            R.color.colorGrayBtnEnable
                        )
                }
                Status.SUCCESS -> {
                    binding.progressbarSignUp.invisible()
                    showFragment(R.id.frmLogin, SignInFragment(), "LoginFragment")
                    viewModel.resultData.value = null
                }
            }
        })
    }

    override fun afterTextChanged(s: Editable?) {
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        if (binding.edtEmail.getTextTrim() != ""
            && binding.edtUserName.getTextTrim() != ""
            && binding.edtDayOfBirth.getTextTrim() != ""
            && binding.edtPassword.getTextTrim() != ""
            && binding.edtConfirmPassword.getTextTrim() != ""
        ) {
            binding.btnSignUp.isEnabled = true
            binding.btnSignUp.backgroundTintList =
                ContextCompat.getColorStateList(
                    requireContext(),
                    R.color.colorOrange
                )
        } else {
            binding.btnSignUp.isEnabled = false
            binding.btnSignUp.backgroundTintList =
                ContextCompat.getColorStateList(
                    requireContext(),
                    R.color.colorGrayBtnEnable
                )
        }
    }

}
