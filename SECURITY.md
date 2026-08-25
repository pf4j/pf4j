# Security Policy

## Supported versions

Security fixes are applied to the latest 3.x release. Older releases are not
backported, so if you are affected the fix is to upgrade.

## Reporting a vulnerability

Please report privately, not through a public issue:

**[Report a vulnerability](https://github.com/pf4j/pf4j/security/advisories/new)**

This opens a private advisory visible only to you and the maintainers. If the
report is confirmed, the fix is developed privately and a security advisory is
published when the fix is released, so that downstream projects are notified
automatically.

PF4J is maintained by a small number of people in their own time. You will
normally get a first response within a few days. Please allow reasonable time
for a fix before disclosing publicly.

## Scope

PF4J loads and executes third-party code. That is what it is for. Separate
class loaders isolate plugins from classpath conflicts, **not** from malicious
behaviour: a plugin runs with the same JVM privileges as the application that
loads it, and modern JDKs no longer provide an in-process sandbox to fall back
on.

Deciding which plugins may be loaded, where they come from, and what the
process is allowed to reach is the responsibility of the host application.
Reports that amount to "a plugin can do whatever the host application can do"
describe intended behaviour and are not vulnerabilities.

In scope, for example:

- a plugin escaping the isolation PF4J does claim to provide, such as reading
  or overriding classes of another plugin that should not be reachable
- a crafted plugin archive causing path traversal, arbitrary file write, or
  code execution during loading, before the plugin is ever started
- a denial of service triggered by a malformed plugin descriptor or archive
- a vulnerability in one of PF4J's own dependencies as used by PF4J
