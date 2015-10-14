// im.tox.tox4j.crypto.ToxCryptoConstants$
void
checkToxCryptoConstants ()
{
  static_assert (TOX_PASS_ENCRYPTION_EXTRA_LENGTH == 80, "Java constant out of sync with C");
  static_assert (TOX_PASS_HASH_LENGTH == 32, "Java constant out of sync with C");
  static_assert (TOX_PASS_SALT_LENGTH == 32, "Java constant out of sync with C");
}
