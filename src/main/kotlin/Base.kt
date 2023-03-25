package contacts

import kotlinx.serialization.Serializable
import java.text.SimpleDateFormat
import java.util.*

@Serializable
sealed class Base(var baseName: String, var baseNumber: String) {
    val timeCreated: String = SimpleDateFormat("yyyy-MM-dd'T'HH:mm").format(Date())
    var timeLastEdit: String = timeCreated
    abstract fun fullInfo()
    fun search(value: String): Boolean {
        for (field in this::class.java.declaredFields) {
            field.isAccessible = true
            val fieldValue = field.get(this)?.toString() ?: ""
            if (fieldValue.contains(value, true)) return true
        }
        return false
    }
}

fun Base.setProperty(element: Any, fieldElement: String, value: String) {
    val propertySet = element::class.java.getDeclaredField(fieldElement)
    propertySet.isAccessible = true
    propertySet.set(this, if (fieldElement == "number") checkNumber(value) else value)
}

@Serializable
class Person(
    private val name: String, private val surname: String, private val birthDate: String,
    private val gender: String, private var number: String
) : Base(name, number) {
    companion object {
        fun createPerson() = Person(
            input("Enter the name:"), input("Enter the surname:"), checkDate(input("Enter the birth date:")),
            checkGender(input("Enter the gender (M, F):")), checkNumber(input("Enter the number:"))
        )
    }

    override fun toString(): String = "$name $surname"
    override fun fullInfo() = println(
        "Name: $name\nSurname: $surname\nBirth date: $birthDate\nGender: $gender\n" +
                "Number: $number\nTime created: $timeCreated\nTime last edit: $timeLastEdit\n"
    )
}

@Serializable
class Organization(
    private val name: String, private val address: String, private var number: String
) : Base(name, number) {
    companion object {
        fun createOrganization() = Organization(
            input("Enter the organization name:"), input("Enter the address:"), checkNumber(input("Enter the number:"))
        )
    }

    override fun toString(): String = name
    override fun fullInfo() = println(
        "Organization name: $name\nAddress: $address\nNumber: $number\n" +
                "Time created: $timeCreated\nTime last edit: $timeLastEdit\n"
    )
}