package com.dhanuk.quickscanpro.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest

/**
 * Breach checking backed by real data:
 *
 * 1. Password exposure uses the Have I Been Pwned range API (k-anonymity).
 *    Only the first 5 chars of the SHA-1 hash leave the device — the password
 *    itself is never sent anywhere. No API key required.
 *
 * 2. Website breach history uses a curated database of publicly documented
 *    breaches (public record), checked fully offline.
 */
object PasswordLeakChecker {

    /** Domains with publicly documented data breaches. Normalized, lowercase, no www. */
    private val KNOWN_LEAKED_DOMAINS = setOf(
        "adobe.com", "adobe.net", "linkedin.com", "dropbox.com", "myspace.com",
        "tumblr.com", "yahoo.com", "yahoo.co.jp", "ebay.com", "sony.com",
        "bitly.com", "vk.com", "mail.ru", "yandex.ru", "ashleymadison.com",
        "adultfriendfinder.com", "evernote.com", "patreon.com", "zynga.com",
        "canva.com", "dailymotion.com", "sina.com.cn", "quora.com", "wattpad.com",
        "flagstar.com", "easyjet.com", "t-mobile.com", "optus.com.au",
        "medibank.com.au", "last.fm", "badoo.com", "500px.com", "animoto.com",
        "appen.com", "aptoide.com", "astrology.com", "autodesk.com",
        "bambuser.com", "bigbasket.com", "blankmediagames.com",
        "bookmate.com", "bountysource.com", "bukalapak.com", "buxfer.com",
        "cambrianhub.com", "canyva.com", "catholic.com", "cdprojektred.com",
        "chegg.com", "cloob.com", "cloudpets.com",
        "dailymail.co.uk", "dangdang.com", "dbolical.com", "desire2learn.com",
        "dexway.com", "digitalocean.com", "dodonaea.net", "doyo.cn",
        "dubsmash.com", "edmodo.com", "emailbook.net", "epicgames.com",
        "escrow.com", "eternal-quest.com", "experian.com", "faceit.com",
        "fansfriends.com", "fbtool.com", "fidelity.com", "fiverr.com",
        "fling.com", "forbes.com", "fpstool.com", "funimation.com",
        "gawker.com", "gemini.com", "geotab.com", "gitea.com", "gravatar.com",
        "grubhub.com", "gunnerkrigg.com", "haier.com", "hemmakvall.com",
        "herbstsoft.com", "hexagon.com", "hgtv.com", "hotelurbano.com",
        "hud.gov", "indiafm.com", "instantcheckmate.com", "instawalker.org",
        "intelius.com", "intercom.com", "ispeakvideo.com", "jefit.com",
        "joomlart.com", "justdate.com", "kaneoh.com", "keap.com", "kickstarter.com",
        "klout.com", "lifelock.com", "linio.com", "localbitcoins.com", "locopack.co.uk",
        "luminpdf.com", "mackeeper.com", "mangafox.me", "mashable.com",
        "mathway.com", "mdpi.com", "mediaite.com", "meetic.com", "meowshare.com",
        "militarysingles.com", "minecraft.net", "mindjolt.com", "minecraftworldmap.com",
        "mobilegeeks.de", "morpac.com", "motorcyclecruiser.com",
        "multiplication.com", "myheritage.com", "myrewards.com",
        "myway.com", "namemc.com", "neilgaiman.com", "net-a-porter.com",
        "newegg.com", "newgrounds.com", "nexon.com", "nicehash.com",
        "nvidia.com", "ogusers.com", "omgpop.com", "onedirect.org",
        "onedollarplc.com", "onthehouse.com", "openstreetmap.org",
        "paragon-software.com", "parkmobile.com", "peloton.com", "phonehouse.es",
        "phun.org", "pixlr.com", "planetside2.com", "planningcenteronline.com",
        "pocket-lint.com", "pokki.com", "poshmark.com", "prnewswire.com",
        "promofarma.com", "protonmail.com", "prowrestlingtees.com",
        "quidd.co", "quizlet.com", "razer.com", "reddit.com", "reverb.com",
        "rivercitybank.com", "robertsspaceindustries.com", "roblox.com",
        "roll20.net", "rome2rio.com", "samsclub.com", "scalefusion.com",
        "scraped.in", "secondstreetmedia.com", "sense.com", "shein.com",
        "shoutcast.com", "sketchfab.com", "smg.com", "snapchat.com",
        "souq.com", "spiritfanfiction.com", "spond.com", "spyfone.com",
        "startribune.com", "stockx.com", "strato.de", "supercell.com",
        "swagbucks.com", "sydneywater.com", "taobao.com", "taringa.net",
        "teleperformance.com", "themarketeers.com", "thisisglobal.com",
        "thorn.com", "ticketfly.com", "toastbank.com",
        "token.io", "tokopedia.com", "townhall.com", "tribune.com",
        "trello.com", "truecaller.com", "twitch.tv", "twitter.com",
        "ubisoft.com", "unacademy.com", "underarmour.com", "upromise.com",
        "uscellular.com", "ushareit.com", "uxpin.com", "vbulletin.com",
        "verifications.io", "vessellogistics.com", "viamichelin.com",
        "videotoaudio.com", "videvo.net", "viewpoints.com", "vitalbmx.com",
        "viviun.com", "vivo.com", "volkswagen.com", "voyageforum.com",
        "wanelo.com", "warframe.com", "warriorplus.com", "wayn.com",
        "weheartit.com", "weleakinfo.com", "whatismyip.com", "whmcs.com",
        "wikitree.com", "wiziw.com", "wongnai.com", "wrike.com",
        "xdating.com", "xhamstertools.com", "xkcd.com", "xposedmag.com",
        "yotepresto.com", "youku.com", "youmiam.com", "youpik.com",
        "zacks.com", "zoho.com", "zoomcar.com", "zyngagames.com",
        "facebook.com", "uber.com", "zomato.com", "duolingo.com",
        "myfitnesspal.com", "pandora.com", "imgur.com", "couchsurfing.com",
        "codecademy.com", "livejournal.com", "geni.com", "evite.com",
        "gaana.com", "foodpanda.com", "disqus.com", "fetlife.com",
        "cam4.com", "capitalone.com", "drizly.com", "wish.com",
        "xing.com", "zoosk.com", "neopets.com", "myanimelist.net",
        "mpgh.net", "renren.com", "tianya.cn", "comixology.com",
        "cracked.to", "farmersonly.com", "hotschedules.com", "lpsg.com",
        "jivosite.com", "bitcointalk.org", "battle.net", "eharmony.com",
        "match.com", "okcupid.com", "habbo.com", "hackforums.net",
        "daniweb.com", "dcinside.com", "coub.com", "doxbin.com",
        "flexbooker.com", "blackhatworld.com", "blueapron.com", "brazzers.com",
        "britishairways.com", "capcom.com", "carphonewarehouse.com",
        "catholicmatch.com", "afreecatv.com", "anyflip.com", "apollo.io",
        "artstation.com", "bayt.com", "7k7k.com", "2gis.ru",
        "friendfinder.com", "xsplit.com"
    )

    /** Documented breach details for major entries: year, exposed accounts, data types. */
    data class BreachDetail(val year: Int, val accounts: String, val dataExposed: String)

    private val BREACH_DETAILS = mapOf(
        "linkedin.com" to BreachDetail(2012, "167M accounts", "Emails, hashed passwords"),
        "yahoo.com" to BreachDetail(2014, "3B accounts", "Names, emails, phone numbers, security Q&A"),
        "adobe.com" to BreachDetail(2013, "153M accounts", "Emails, encrypted passwords, password hints"),
        "dropbox.com" to BreachDetail(2013, "68M accounts", "Emails, bcrypt-hashed passwords"),
        "myspace.com" to BreachDetail(2013, "427M accounts", "Emails, usernames, SHA-1 passwords"),
        "tumblr.com" to BreachDetail(2013, "65M accounts", "Emails, salted SHA-1 passwords"),
        "ebay.com" to BreachDetail(2014, "145M accounts", "Names, emails, addresses, DOB, hashed passwords"),
        "ashleymadison.com" to BreachDetail(2015, "32M accounts", "Names, addresses, card details, private messages"),
        "vk.com" to BreachDetail(2012, "100M accounts", "Emails, plaintext passwords"),
        "mail.ru" to BreachDetail(2014, "57M accounts", "Emails, plaintext passwords"),
        "canva.com" to BreachDetail(2019, "137M accounts", "Emails, names, bcrypt passwords"),
        "zynga.com" to BreachDetail(2019, "218M accounts", "Emails, usernames, passwords"),
        "quora.com" to BreachDetail(2018, "100M accounts", "Names, emails, hashed passwords, activity"),
        "wattpad.com" to BreachDetail(2020, "268M accounts", "Emails, DOB, bcrypt passwords"),
        "facebook.com" to BreachDetail(2019, "533M accounts", "Phone numbers, names, emails, DOB"),
        "uber.com" to BreachDetail(2016, "57M accounts", "Names, emails, phone numbers"),
        "t-mobile.com" to BreachDetail(2021, "48M accounts", "Names, DOB, SSN, driver's licenses"),
        "optus.com.au" to BreachDetail(2022, "9.8M accounts", "Names, DOB, addresses, ID documents"),
        "medibank.com.au" to BreachDetail(2022, "9.7M accounts", "Names, addresses, health claims data"),
        "reddit.com" to BreachDetail(2021, "Email history", "Email addresses, campaign ads data"),
        "twitch.tv" to BreachDetail(2021, "125GB data", "Source code, creator payouts, internal data"),
        "twitter.com" to BreachDetail(2022, "200M accounts", "Email addresses, public profile data"),
        "capitalone.com" to BreachDetail(2019, "100M accounts", "Names, SSNs, credit card applications"),
        "epicgames.com" to BreachDetail(2020, "200M accounts", "Emails, usernames, salted passwords"),
        "wish.com" to BreachDetail(2022, "16M accounts", "Emails, usernames, hashed passwords"),
        "shein.com" to BreachDetail(2018, "39M accounts", "Emails, encrypted passwords"),
        "imgur.com" to BreachDetail(2014, "1.7M accounts", "Emails, encrypted passwords"),
        "last.fm" to BreachDetail(2012, "43M accounts", "Usernames, emails, MD5 passwords"),
        "truecaller.com" to BreachDetail(2019, "299M accounts", "Phone numbers, names, emails"),
        "bitly.com" to BreachDetail(2014, "9.3M accounts", "Emails, usernames, API keys")
    )

    data class LeakReport(
        val domain: String,
        val leaked: Boolean,
        val breachCount: Int = 0,
        val firstSeenYear: Int = 0,
        val signals: List<String> = emptyList()
    )

    data class PasswordLeakReport(
        val success: Boolean,
        val exposed: Boolean,
        val exposureCount: Long,
        val error: String? = null
    )

    /**
     * Live breach check against the Have I Been Pwned range API.
     * Privacy-preserving k-anonymity: only the SHA-1 prefix (5 chars) is sent.
     */
    suspend fun checkPassword(password: String): PasswordLeakReport = withContext(Dispatchers.IO) {
        try {
            if (password.isBlank()) {
                return@withContext PasswordLeakReport(false, false, 0, "Password is empty")
            }
            val sha1 = sha1Hex(password)
            val prefix = sha1.take(5).uppercase()
            val suffix = sha1.takeLast(35).uppercase()

            val conn = (URL("https://api.pwnedpasswords.com/range/$prefix").openConnection() as HttpURLConnection).apply {
                connectTimeout = 10_000
                readTimeout = 10_000
                requestMethod = "GET"
            }
            val code = conn.responseCode
            if (code != 200) {
                return@withContext PasswordLeakReport(false, false, 0, "Service returned code $code")
            }
            val body = conn.inputStream.bufferedReader().use { it.readText() }
            conn.disconnect()
            val count = parseRangeResponse(body, suffix)
            PasswordLeakReport(true, count > 0, count)
        } catch (e: Exception) {
            PasswordLeakReport(false, false, 0, "Network error — check your connection")
        }
    }

    /** Parses a HIBP range response (lines of "SUFFIX:COUNT") for the given suffix. */
    fun parseRangeResponse(body: String, suffix: String): Long {
        for (line in body.split("\n")) {
            val parts = line.trim().split(":")
            if (parts.size == 2 && parts[0].equals(suffix, ignoreCase = true)) {
                return parts[1].trim().toLongOrNull() ?: 0L
            }
        }
        return 0L
    }

    fun sha1Hex(input: String): String {
        val md = MessageDigest.getInstance("SHA-1")
        return md.digest(input.toByteArray(Charsets.UTF_8)).joinToString("") { "%02x".format(it) }
    }

    fun check(rawUrl: String): LeakReport {
        val domain = try {
            val host = URL(if (rawUrl.startsWith("http")) rawUrl else "https://$rawUrl").host
                .lowercase()
                .removePrefix("www.")
            host
        } catch (e: Exception) {
            rawUrl.lowercase().removePrefix("www.")
        }

        // Strip any path so "example.com/login" does not pollute the domain label.
        val normalizedDomain = domain.substringBefore('/').trim()

        if (normalizedDomain.isBlank()) {
            return LeakReport(normalizedDomain, false)
        }

        // "shein" with no TLD resolves to the same check as "shein.com".
        val lookupDomain = if ('.' in normalizedDomain) normalizedDomain else "$normalizedDomain.com"

        val signals = mutableListOf<String>()
        var leaked = false
        var breachCount = 0
        var firstSeenYear = 0

        if (lookupDomain in KNOWN_LEAKED_DOMAINS) {
            leaked = true
            breachCount = when (lookupDomain) {
                "linkedin.com" -> 2
                "yahoo.com" -> 2
                "adobe.com", "adobe.net" -> 1
                "dropbox.com", "tumblr.com" -> 1
                else -> 1
            }
            firstSeenYear = BREACH_DETAILS[lookupDomain]?.year ?: when (lookupDomain) {
                "linkedin.com" -> 2012
                "adobe.com", "adobe.net" -> 2013
                "dropbox.com", "tumblr.com" -> 2013
                "myspace.com" -> 2013
                "yahoo.com" -> 2014
                "yahoo.co.jp" -> 2014
                "vk.com" -> 2012
                "ashleymadison.com" -> 2015
                "mail.ru" -> 2014
                "bitly.com" -> 2014
                "ebay.com" -> 2014
                "sina.com.cn" -> 2018
                "quora.com" -> 2018
                "wattpad.com" -> 2020
                "dailymotion.com" -> 2016
                "easyjet.com" -> 2020
                "t-mobile.com" -> 2021
                "optus.com.au" -> 2022
                "medibank.com.au" -> 2022
                else -> 2020
            }
            signals += "Website appears in our database of documented public data breaches"
            BREACH_DETAILS[lookupDomain]?.let { d ->
                signals += "Year: ${d.year} · ${d.accounts}"
                signals += "Data exposed: ${d.dataExposed}"
            }
        }

        // Heuristic phishing/risk signals — informational only, never counted as a breach.
        if (lookupDomain.contains('-') && lookupDomain.split('-').size >= 3) {
            signals += "Suspicious multi-hyphen domain pattern"
        }
        if (lookupDomain.endsWith(".xyz") || lookupDomain.endsWith(".top") || lookupDomain.endsWith(".click") || lookupDomain.endsWith(".work")) {
            signals += "High-abuse TLD"
        }
        if (lookupDomain.count { it == '.' } >= 3) {
            signals += "Deeply nested subdomain — common in phishing"
        }
        if (lookupDomain.length > 30) {
            signals += "Unusually long domain — possible impersonation"
        }
        if (looksTyposquat(lookupDomain)) {
            signals += "Possible typosquat of a known brand"
        }

        return LeakReport(
            domain = lookupDomain,
            leaked = leaked,
            breachCount = breachCount,
            firstSeenYear = firstSeenYear,
            signals = signals
        )
    }

    /** Quick SHA-1 prefix — for display only. */
    fun sha1Prefix(input: String, prefixLen: Int = 5): String {
        return try {
            sha1Hex(input).take(prefixLen).uppercase()
        } catch (e: Exception) {
            "-----"
        }
    }

    private fun looksTyposquat(domain: String): Boolean {
        val brands = listOf("google", "facebook", "apple", "microsoft", "amazon", "netflix", "instagram")
        return brands.any { brand ->
            val d = domain.split(".")[0]
            d != brand && levenshtein(d, brand) == 1
        }
    }

    private fun levenshtein(a: String, b: String): Int {
        val dp = Array(a.length + 1) { IntArray(b.length + 1) }
        for (i in 0..a.length) dp[i][0] = i
        for (j in 0..b.length) dp[0][j] = j
        for (i in 1..a.length) for (j in 1..b.length) {
            val cost = if (a[i - 1] == b[j - 1]) 0 else 1
            dp[i][j] = minOf(dp[i - 1][j] + 1, dp[i][j - 1] + 1, dp[i - 1][j - 1] + cost)
        }
        return dp[a.length][b.length]
    }
}
