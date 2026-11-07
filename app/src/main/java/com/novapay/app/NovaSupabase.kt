package com.novapay.app

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.auth.currentSessionOrNull

/**
 * Shared Supabase client for NOVA PAY.
 *
 * The publishable key is intended for client applications. Database access
 * must still be protected by SQL grants and Row Level Security policies.
 * Never place a Supabase secret/service_role key in this Android app.
 */
object NovaSupabase {
    fun currentUser(): AuthUser? {
        val user = client.auth.currentSessionOrNull()?.user ?: return null
        val metadata = user.userMetadata
        return AuthUser(
            name = metadata?.get("display_name")?.toString()?.trim('"')?.takeIf { it.isNotBlank() } ?: "NOVA MEMBER",
            email = user.email ?: "",
            phone = metadata?.get("phone")?.toString()?.trim('"') ?: "",
            phoneVerified = false
        )
    }

    val client = createSupabaseClient(
        supabaseUrl = BuildConfig.SUPABASE_URL,
        supabaseKey = BuildConfig.SUPABASE_PUBLISHABLE_KEY
    ) {
        install(Auth)
        install(Postgrest)
    }
}
