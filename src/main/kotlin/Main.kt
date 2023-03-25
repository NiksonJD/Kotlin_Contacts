package contacts

import kotlinx.serialization.json.Json
import java.io.File
import kotlinx.serialization.*
import java.text.SimpleDateFormat
import java.util.*

val contacts = mutableListOf<Base>()
lateinit var filePath: File
val menuMap = mutableMapOf(
    "add" to ::addElement, "list" to ::listContacts, "search" to ::searchElements,
    "count" to ::countElements
)

fun input(prompt: String) = println(prompt).run { readln() }

fun checkNumber(number: String): String =
    if (number.matches(Regex("""\+?[\s-]?((\(\w+\)[\s-]\w{2,})|(\w+[\s-]\(\w{2,}\))|(\(?\w+\)?))([\s-]\w{2,})*"""))) {
        number
    } else "[no number]".also { println("Wrong number format!") }

fun checkDate(date: String) = date.ifBlank { "[no data]".also { println("Bad birth date!") } }

fun checkGender(gender: String) = if (gender in setOf("M", "F")) gender else "[no data]".also { println("Bad gender!") }

fun addElement() {
    val type = (input("Enter the type (person, organization):"))
    if (type == "person") contacts.add(Person.createPerson()) else contacts.add(Organization.createOrganization())
    println("The record added.\n").also { toFile() }
}

fun listContacts() {
    contacts.forEachIndexed { i, p -> println("${i + 1}. $p") }
    val menuNumber = input("[list] Enter action ([number], back):")
    if (menuNumber != "back") menuRecord(contacts.getOrNull(menuNumber.toInt().dec()))
}

fun searchElements() {
    val filtered = input("Enter search query:").let { q -> contacts.filter { it.search(q) } }
    println("Found ${filtered.size} result${if (filtered.size > 1) "s" else ""}:")
    filtered.forEachIndexed { i, p -> println("${i + 1}. $p") }
    when (val menuSearch = input("[search] Enter action ([number], back, again):")) {
        "again" -> searchElements()
        "back" -> {}
        else -> menuRecord(filtered.getOrNull(menuSearch.toInt().dec()))
    }
}

fun countElements() = println("The Phone Book has ${contacts.size} records.\n")

fun menuRecord(record: Base?) {
    record?.fullInfo()
    when (input("[record] Enter action (edit, delete, menu):")) {
        "delete" -> contacts.remove(record).also { toFile() }
        "menu" -> {}
        "edit" -> editElement(record)
    }
}

fun editElement(element: Base?) {
    if (element is Person) {
        val fieldElement = input("Select a field (name, surname, birth, gender, number):")
        element.setProperty(element, fieldElement, input("Enter $fieldElement:"))
    } else if (element is Organization) {
        val fieldElement = input("Select a field (name, address, number):")
        element.setProperty(element, fieldElement, input("Enter $fieldElement:"))
    }
    element?.timeLastEdit = SimpleDateFormat("yyyy-MM-dd'T'HH:mm").format(Date())
    println("Saved\n").also { element?.fullInfo() }.also { toFile() }
}

fun toFile() {
    val serialized = Json.encodeToString(contacts).also { filePath.writeText("") }
    filePath.writeText(serialized)
}

fun userInput() {
    while (true) {
        when (val i = input("[menu] Enter action (add, list, search, count, exit):")) {
            "exit" -> break
            "list", "search" -> if (contacts.isEmpty()) println("No records to $i!") else menuMap[i]?.invoke()
            else -> menuMap[i]?.invoke()
        }
    }
}

fun main(args: Array<String>) {
    filePath = args.getOrNull(0)?.let { File(it) } ?: File("phonebook.db")
    if (filePath.exists()) contacts.addAll(Json.decodeFromString<MutableList<Base>>(filePath.readText()))
    userInput()
}