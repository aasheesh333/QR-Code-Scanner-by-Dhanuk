package com.dhanuk.quickscanpro.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TimelineBuilderTest {

    @Test
    fun build_emptyList_returnsEmpty() {
        val result = TimelineBuilder.build(emptyList())
        assertTrue(result.isEmpty())
    }

    @Test
    fun build_singleScan_returnsSingleDay() {
        val now = System.currentTimeMillis()
        val scan = createScan(content = "test", timestamp = now)
        val result = TimelineBuilder.build(listOf(scan))
        assertEquals(1, result.size)
        assertEquals("Today", result[0].dayLabel)
        assertEquals(1, result[0].items.size)
    }

    @Test
    fun build_multipleScansSameDay_groupsIntoSingleDay() {
        val now = System.currentTimeMillis()
        val scans = listOf(
            createScan(content = "a", timestamp = now),
            createScan(content = "b", timestamp = now - 1000),
            createScan(content = "c", timestamp = now - 2000)
        )
        val result = TimelineBuilder.build(scans)
        assertEquals(1, result.size)
        assertEquals(3, result[0].items.size)
    }

    @Test
    fun build_scansOnDifferentDays_createsMultipleDays() {
        val now = System.currentTimeMillis()
        val yesterday = now - 86400000L
        val scans = listOf(
            createScan(content = "today", timestamp = now),
            createScan(content = "yesterday", timestamp = yesterday)
        )
        val result = TimelineBuilder.build(scans)
        assertEquals(2, result.size)
    }

    @Test
    fun build_scansSortedByTimestampDescending() {
        val now = System.currentTimeMillis()
        val scans = listOf(
            createScan(content = "old", timestamp = now - 5000),
            createScan(content = "new", timestamp = now)
        )
        val result = TimelineBuilder.build(scans)
        assertEquals("new", result[0].items[0].content)
        assertEquals("old", result[0].items[1].content)
    }

    @Test
    fun build_daysSortedByMostRecentFirst() {
        val now = System.currentTimeMillis()
        val twoDaysAgo = now - (2 * 86400000L)
        val scans = listOf(
            createScan(content = "older", timestamp = twoDaysAgo),
            createScan(content = "recent", timestamp = now)
        )
        val result = TimelineBuilder.build(scans)
        assertEquals("recent", result[0].items.first().content)
        assertEquals("older", result[1].items.first().content)
    }

    @Test
    fun build_yesterdayLabel_correct() {
        val yesterday = System.currentTimeMillis() - 86400000L
        val scan = createScan(content = "test", timestamp = yesterday)
        val result = TimelineBuilder.build(listOf(scan))
        assertEquals("Yesterday", result[0].dayLabel)
    }

    @Test
    fun build_dateLabelFormatted() {
        val now = System.currentTimeMillis()
        val scan = createScan(content = "test", timestamp = now)
        val result = TimelineBuilder.build(listOf(scan))
        assertTrue(result[0].dateLabel.isNotEmpty())
    }

    private fun createScan(content: String, timestamp: Long) =
        com.dhanuk.quickscanpro.database.ScanResult(
            content = content,
            timestamp = timestamp
        )
}
