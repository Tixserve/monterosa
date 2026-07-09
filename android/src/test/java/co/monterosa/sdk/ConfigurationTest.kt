package co.monterosa.sdk

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ConfigurationTest {

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private fun baseMap(
        overrides: Map<String, Any> = emptyMap()
    ): Map<String, Any> = mutableMapOf<String, Any>(
        "host" to "https://example.com",
        "projectId" to "project-123"
    ).apply { putAll(overrides) }

    // -----------------------------------------------------------------------
    // Required fields
    // -----------------------------------------------------------------------

    @Test
    fun `returns null when host is missing`() {
        val map = mapOf<String, Any>("projectId" to "p")
        assertNull(map.toConfiguration())
    }

    @Test
    fun `returns null when projectId is missing`() {
        val map = mapOf<String, Any>("host" to "h")
        assertNull(map.toConfiguration())
    }

    @Test
    fun `returns null when both required fields are missing`() {
        assertNull(emptyMap<String, Any>().toConfiguration())
    }

    @Test
    fun `parses host and projectId`() {
        val config = baseMap().toConfiguration()!!
        assertEquals("https://example.com", config.host)
        assertEquals("project-123", config.projectId)
    }

    @Test
    fun `coerces non-string host via toString`() {
        val map = mapOf<String, Any>("host" to 12345, "projectId" to "p")
        val config = map.toConfiguration()!!
        assertEquals("12345", config.host)
    }

    // -----------------------------------------------------------------------
    // Parameters — regression tests for DFL Captain lang bug
    //
    // Root cause: Kotlin's Iterable.toMap() returns Collections.singletonMap()
    // for single-entry results, which fails `as? HashMap`. These tests ensure
    // parameters are always preserved regardless of entry count.
    // -----------------------------------------------------------------------

    @Test
    fun `REGRESSION - single-entry parameters are preserved`() {
        val config = baseMap(mapOf("parameters" to mapOf("lang" to "de"))).toConfiguration()!!
        assertEquals(hashMapOf("lang" to "de"), config.parameters)
        assertEquals(1, config.parameters.size)
    }

    @Test
    fun `empty parameters map is preserved`() {
        val config = baseMap(mapOf("parameters" to emptyMap<String, String>())).toConfiguration()!!
        assertEquals(hashMapOf<String, String>(), config.parameters)
        assertTrue(config.parameters.isEmpty())
    }

    @Test
    fun `multi-entry parameters are preserved`() {
        val params = mapOf("lang" to "de", "theme" to "dark", "region" to "eu")
        val config = baseMap(mapOf("parameters" to params)).toConfiguration()!!
        assertEquals(HashMap(params), config.parameters)
        assertEquals(3, config.parameters.size)
    }

    @Test
    fun `defaults to empty parameters when key is absent`() {
        val config = baseMap().toConfiguration()!!
        assertTrue(config.parameters.isEmpty())
    }

    @Test
    fun `defaults to empty parameters when value is null type`() {
        // RN bridge may pass null for absent optional fields after toNonNullMap filtering,
        // but if somehow a non-Map type is passed, it should default to empty.
        val map = baseMap(mapOf("parameters" to "not-a-map"))
        val config = map.toConfiguration()!!
        assertTrue(config.parameters.isEmpty())
    }

    @Test
    fun `filters non-string values from parameters`() {
        val mixedParams: Map<Any?, Any?> = mapOf("lang" to "de", "count" to 42, "flag" to true)
        val config = baseMap(mapOf("parameters" to mixedParams)).toConfiguration()!!
        // Only string values should survive
        assertEquals(hashMapOf("lang" to "de"), config.parameters)
    }

    @Test
    fun `filters non-string keys from parameters`() {
        val mixedParams: Map<Any?, Any?> = mapOf("lang" to "de", 123 to "numeric-key")
        val config = baseMap(mapOf("parameters" to mixedParams)).toConfiguration()!!
        assertEquals(hashMapOf("lang" to "de"), config.parameters)
    }

    @Test
    fun `parameters result is a HashMap instance`() {
        val config = baseMap(mapOf("parameters" to mapOf("lang" to "de"))).toConfiguration()!!
        assertTrue(config.parameters is HashMap)
    }

    // -----------------------------------------------------------------------
    // Optional string fields
    // -----------------------------------------------------------------------

    @Test
    fun `parses eventId`() {
        val config = baseMap(mapOf("eventId" to "event-456")).toConfiguration()!!
        assertEquals("event-456", config.eventId)
    }

    @Test
    fun `eventId is null when absent`() {
        assertNull(baseMap().toConfiguration()!!.eventId)
    }

    @Test
    fun `parses token`() {
        val config = baseMap(mapOf("token" to "jwt-abc")).toConfiguration()!!
        assertEquals("jwt-abc", config.token)
    }

    @Test
    fun `token is null when absent`() {
        assertNull(baseMap().toConfiguration()!!.token)
    }

    @Test
    fun `parses experienceUrl`() {
        val config = baseMap(mapOf("experienceUrl" to "https://custom.url")).toConfiguration()!!
        assertEquals("https://custom.url", config.experienceUrl)
    }

    @Test
    fun `experienceUrl is null when absent`() {
        assertNull(baseMap().toConfiguration()!!.experienceUrl)
    }

    // -----------------------------------------------------------------------
    // backgroundColor
    // Note: toColorInt() requires Android framework, so color parsing tests
    // must run as Android instrumented tests. We only test the null-absent case.
    // -----------------------------------------------------------------------

    @Test
    fun `backgroundColor is null when absent`() {
        assertNull(baseMap().toConfiguration()!!.backgroundColor)
    }

    // -----------------------------------------------------------------------
    // Boolean defaults — must match what JS layer documents
    // -----------------------------------------------------------------------

    @Test
    fun `autoresizesHeight defaults to false`() {
        assertEquals(false, baseMap().toConfiguration()!!.autoresizesHeight)
    }

    @Test
    fun `hidesHeadersAndFooters defaults to true`() {
        assertEquals(true, baseMap().toConfiguration()!!.hidesHeadersAndFooters)
    }

    @Test
    fun `allowsPopupBehavior defaults to false`() {
        assertEquals(false, baseMap().toConfiguration()!!.allowsPopupBehavior)
    }

    @Test
    fun `showsDefaultShareSheet defaults to true`() {
        assertEquals(true, baseMap().toConfiguration()!!.showsDefaultShareSheet)
    }

    @Test
    fun `launchesURLsWithBlankTargetToBrowser defaults to true`() {
        assertEquals(true, baseMap().toConfiguration()!!.launchesURLsWithBlankTargetToBrowser)
    }

    @Test
    fun `boolean overrides are respected`() {
        val config = baseMap(
            mapOf(
                "autoresizesHeight" to true,
                "hidesHeadersAndFooters" to false,
                "allowsPopupBehavior" to true,
                "showsDefaultShareSheet" to false,
                "launchesURLsWithBlankTargetToBrowser" to false
            )
        ).toConfiguration()!!
        assertEquals(true, config.autoresizesHeight)
        assertEquals(false, config.hidesHeadersAndFooters)
        assertEquals(true, config.allowsPopupBehavior)
        assertEquals(false, config.showsDefaultShareSheet)
        assertEquals(false, config.launchesURLsWithBlankTargetToBrowser)
    }

    // -----------------------------------------------------------------------
    // isDifferentExperienceThan
    //
    // Fields that trigger recreation: host, projectId, eventId, experienceUrl, parameters
    // Fields that do NOT trigger recreation: token, booleans, backgroundColor
    // -----------------------------------------------------------------------

    @Test
    fun `isDifferent returns true when previous is null`() {
        assertTrue(baseMap().toConfiguration()!!.isDifferentExperienceThan(null))
    }

    @Test
    fun `isDifferent returns false for identical configs`() {
        val config = baseMap(mapOf("parameters" to mapOf("lang" to "de"))).toConfiguration()!!
        assertEquals(false, config.isDifferentExperienceThan(config))
    }

    @Test
    fun `isDifferent returns true when host changes`() {
        val a = baseMap().toConfiguration()!!
        val b = baseMap(mapOf("host" to "https://other.com")).toConfiguration()!!
        assertTrue(a.isDifferentExperienceThan(b))
    }

    @Test
    fun `isDifferent returns true when projectId changes`() {
        val a = baseMap().toConfiguration()!!
        val b = baseMap(mapOf("projectId" to "other-project")).toConfiguration()!!
        assertTrue(a.isDifferentExperienceThan(b))
    }

    @Test
    fun `isDifferent returns true when eventId changes`() {
        val a = baseMap(mapOf("eventId" to "e1")).toConfiguration()!!
        val b = baseMap(mapOf("eventId" to "e2")).toConfiguration()!!
        assertTrue(a.isDifferentExperienceThan(b))
    }

    @Test
    fun `isDifferent returns true when experienceUrl changes`() {
        val a = baseMap(mapOf("experienceUrl" to "https://a.com")).toConfiguration()!!
        val b = baseMap(mapOf("experienceUrl" to "https://b.com")).toConfiguration()!!
        assertTrue(a.isDifferentExperienceThan(b))
    }

    @Test
    fun `isDifferent returns true when parameters change`() {
        val a = baseMap(mapOf("parameters" to mapOf("lang" to "de"))).toConfiguration()!!
        val b = baseMap(mapOf("parameters" to mapOf("lang" to "en"))).toConfiguration()!!
        assertTrue(a.isDifferentExperienceThan(b))
    }

    @Test
    fun `isDifferent returns false when only token changes`() {
        val a = baseMap(mapOf("token" to "token-a")).toConfiguration()!!
        val b = baseMap(mapOf("token" to "token-b")).toConfiguration()!!
        assertEquals(false, a.isDifferentExperienceThan(b))
    }

    @Test
    fun `isDifferent returns false when only booleans change`() {
        val a = baseMap(mapOf("autoresizesHeight" to true)).toConfiguration()!!
        val b = baseMap(mapOf("autoresizesHeight" to false)).toConfiguration()!!
        assertEquals(false, a.isDifferentExperienceThan(b))
    }

    // backgroundColor change test omitted — toColorInt() requires Android framework
}
