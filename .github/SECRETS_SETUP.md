# GitHub Secrets Setup Guide

This guide explains how to configure the required GitHub secrets for automated Maven Central deployment.

## Required Secrets

You need to configure 4 secrets in your GitHub repository:

| Secret Name | Description | Where to get it |
|-------------|-------------|-----------------|
| `SONATYPE_USERNAME` | Maven Central user token username | Central Portal |
| `SONATYPE_TOKEN` | Maven Central user token password | Central Portal |
| `GPG_PRIVATE_KEY` | Your GPG private key (ASCII armored) | Local GPG keyring |
| `GPG_PASSPHRASE` | Passphrase for your GPG key | Your GPG key passphrase |

---

## Step 1: Generate Sonatype Central User Token

### 1.1 Access Central Portal

Navigate to: https://central.sonatype.com/account/security

### 1.2 Generate User Token

1. Click **"Generate User Token"** button
2. Copy both values:
   - **Username** (example: `jDtgWD`)
   - **Password** (example: `NlEqYk8KikGmvE0V5xFVgleud2NOL08Aj`)

⚠️ **IMPORTANT:** Save these values immediately! The password will not be shown again.

### 1.3 Add to GitHub Secrets

- `SONATYPE_USERNAME` = the username from token
- `SONATYPE_TOKEN` = the password from token

---

## Step 2: Export GPG Private Key

### 2.1 List Your GPG Keys

```bash
gpg --list-secret-keys --keyid-format LONG
```

Output example:
```
sec   rsa4096/1234567890ABCDEF 2024-01-15 [SC]
      ABCDEF1234567890ABCDEF1234567890ABCDEF12
uid                 [ultimate] Your Name <your.email@example.com>
```

The KEY_ID is: `1234567890ABCDEF` (or the full fingerprint)

### 2.2 Export Private Key

```bash
gpg --export-secret-keys --armor 1234567890ABCDEF
```

This will output something like:
```
-----BEGIN PGP PRIVATE KEY BLOCK-----

lQdGBGXxxxxx...
[many lines of base64 encoded data]
...xxxxx=
-----END PGP PRIVATE KEY BLOCK-----
```

⚠️ **IMPORTANT:** Copy the **entire output** including the BEGIN and END lines.

### 2.3 Add to GitHub Secrets

- `GPG_PRIVATE_KEY` = the entire exported key (including BEGIN/END lines)
- `GPG_PASSPHRASE` = your GPG key passphrase

---

## Step 3: Add Secrets to GitHub

### 3.1 Navigate to Repository Settings

1. Go to your GitHub repository
2. Click **Settings** (top menu)
3. In the left sidebar, click **Secrets and variables** → **Actions**

### 3.2 Add Each Secret

For each of the 4 secrets:

1. Click **"New repository secret"**
2. Enter the **Name** (exactly as shown above)
3. Paste the **Value**
4. Click **"Add secret"**

### 3.3 Verify Secrets

After adding all 4 secrets, you should see:

- ✅ `SONATYPE_USERNAME`
- ✅ `SONATYPE_TOKEN`
- ✅ `GPG_PRIVATE_KEY`
- ✅ `GPG_PASSPHRASE`

---

## Step 4: Test the Setup

### 4.1 Test Snapshot Deployment

1. Go to **Actions** tab in your repository
2. Click **"Deploy Snapshot"** workflow
3. Click **"Run workflow"** button
4. Select branch: `master`
5. Click **"Run workflow"**

The workflow should:
- ✅ Checkout code
- ✅ Set up JDK 17
- ✅ Verify it's a SNAPSHOT version
- ✅ Deploy to Maven Central
- ✅ Show success summary

### 4.2 Test Release (Optional)

⚠️ **WARNING:** This will create a real release! Only do this when ready.

1. Go to **Actions** tab
2. Click **"Release to Maven Central"** workflow
3. Click **"Run workflow"** button
4. Enter:
   - **Release version**: e.g., `3.14.0`
   - **Next development version**: e.g., `3.15.0-SNAPSHOT`
5. Click **"Run workflow"**

---

## Troubleshooting

### Error: "401 Unauthorized"

**Problem:** Sonatype credentials are incorrect or expired.

**Solution:**
1. Generate a new user token at https://central.sonatype.com/account/security
2. Update `SONATYPE_USERNAME` and `SONATYPE_TOKEN` secrets
3. Re-run the workflow

### Error: "gpg: signing failed: Inappropriate ioctl for device"

**Problem:** GPG pinentry configuration issue.

**Solution:** This should already be fixed by the `pom.xml` configuration. If still failing, verify that `maven-gpg-plugin` version is 3.2.7 or higher.

### Error: "gpg: decryption failed: No secret key"

**Problem:** GPG private key not properly imported.

**Solution:**
1. Verify you copied the **entire** GPG key including BEGIN/END lines
2. Make sure there are no extra spaces or line breaks
3. Re-export and update the `GPG_PRIVATE_KEY` secret

### Error: "Not a SNAPSHOT version"

**Problem:** Version in `pom.xml` doesn't end with `-SNAPSHOT`.

**Solution:** This is expected for the snapshot workflow. Either:
- Change version to end with `-SNAPSHOT` in `pom.xml`
- Or use the Release workflow instead

### Workflow Fails on SonarCloud

**Problem:** `SONAR_TOKEN` not configured.

**Solution:** This is expected and won't affect deployments. To fix:
1. Generate token at https://sonarcloud.io/account/security
2. Add `SONAR_TOKEN` secret in GitHub
3. Re-run the workflow

---

## Security Best Practices

### ✅ DO:
- Rotate tokens periodically (every 6-12 months)
- Use strong GPG key passphrases
- Review GitHub Actions logs for any exposed secrets (GitHub auto-masks them)
- Limit repository access to trusted collaborators

### ❌ DON'T:
- Share token values in plaintext
- Commit secrets to the repository
- Use the same GPG key for multiple purposes
- Disable GPG signing

---

## Additional Resources

- [Maven Central Publishing Guide](https://central.sonatype.org/publish/publish-portal-maven/)
- [GitHub Actions Secrets Documentation](https://docs.github.com/en/actions/security-guides/encrypted-secrets)
- [GPG Key Generation Guide](https://central.sonatype.org/publish/requirements/gpg/)

---

## Next Steps

After successfully configuring secrets:

1. ✅ Automatic snapshot deployment will work on every push to `master`
2. ✅ Manual releases can be triggered via GitHub Actions UI
3. ✅ No need for local Maven settings or GPG configuration
4. ✅ Full audit trail in GitHub Actions logs

Happy deploying! 🚀
