package com.wstxda.toolkit.manager.dns

enum class DnsProvider(val hostname: String, val displayName: String) {
    AUTOMATIC("", "Automatic"),
    CLOUDFLARE("1dot1dot1dot1.cloudflare-dns.com", "Cloudflare"),
    GOOGLE("dns.google", "Google"),
    QUAD9("dns.quad9.net", "Quad9"),
    ADGUARD("dns.adguard-dns.com", "AdGuard"),
    NEXT_DNS("dns.nextdns.io", "NextDNS"),
}