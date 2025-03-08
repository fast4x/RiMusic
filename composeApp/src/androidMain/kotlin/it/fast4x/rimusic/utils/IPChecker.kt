package it.fast4x.rimusic.utils

import android.util.Patterns
import java.util.StringTokenizer

const val DELIM = "."

// Return 1 if IP string is valid, else return 0
fun isValidIP(ipStr: String?): Boolean {
    // If the empty string then return false
    if (ipStr == null) return false

    var dots = 0
    val len = ipStr.length
    var count = 0


    // The number dots in the original string should be 3
    // for it to be valid
    for (i in 0 until len) if (ipStr[i] == '.') count++
    if (count != 3) return false


    // Using StringTokenizer to split the IP string
    val st = StringTokenizer(ipStr, DELIM)

    while (st.hasMoreTokens()) {
        val part = st.nextToken()


        // After parsing string, it must be valid
        if (isValidPart(part)) {
            // Parse remaining string
            if (st.hasMoreTokens()) dots++
        } else return false
    }


    // Valid IP string must contain 3 dots
    // This is for cases such as 1...1 where
    // originally the number of dots is three but
    // after iteration of the string, we find it is not valid
    if (dots != 3) return false

    return true
}

// Function to check whether the string passed is valid or not
fun isValidPart(s: String): Boolean {
    val n = s.length


    // If the length of the string is more than 3, then it is not valid
    if (n > 3) return false


    // Check if the string only contains digits
    // If not, then return false
    for (i in 0 until n) if (!(s[i] >= '0' && s[i] <= '9')) return false


    // If the string is "00" or "001" or "05" etc then it is not valid
    if (s.indexOf('0') == 0 && n > 1) return false

    try {
        val x = s.toInt()


        // The string is valid if the number generated is between 0 to 255
        return (x >= 0 && x <= 255)
    } catch (e: NumberFormatException) {
        return false
    }
}

fun isValidUrl(url: String): Boolean {
    return Patterns.WEB_URL.matcher(url).matches()
}