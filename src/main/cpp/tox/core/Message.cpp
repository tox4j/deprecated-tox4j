#include "Message.h"
#include "lwt/logging.h"

#include "IPAddress.h"
#include "KeyPair.h"
#include "Nonce.h"

using namespace tox;


void
MessageBase::append (uint8_t b)
{
  push_back (b & 0xff);
}


void
MessageBase::append (uint16_t s)
{
  push_back ((s >> 8 * 1) & 0xff);
  push_back ((s >> 8 * 0) & 0xff);
}


void
MessageBase::append (uint32_t l)
{
  push_back ((l >> 8 * 3) & 0xff);
  push_back ((l >> 8 * 2) & 0xff);
  push_back ((l >> 8 * 1) & 0xff);
  push_back ((l >> 8 * 0) & 0xff);
}


void
MessageBase::append (uint64_t q)
{
  push_back ((q >> 8 * 7) & 0xff);
  push_back ((q >> 8 * 6) & 0xff);
  push_back ((q >> 8 * 5) & 0xff);
  push_back ((q >> 8 * 4) & 0xff);
  push_back ((q >> 8 * 3) & 0xff);
  push_back ((q >> 8 * 2) & 0xff);
  push_back ((q >> 8 * 1) & 0xff);
  push_back ((q >> 8 * 0) & 0xff);
}

PlainText  &PlainText ::operator << (uint8_t            data) { append (data); return *this; }
PlainText  &PlainText ::operator << (uint16_t           data) { append (data); return *this; }
PlainText  &PlainText ::operator << (uint32_t           data) { append (data); return *this; }
PlainText  &PlainText ::operator << (uint64_t           data) { append (data); return *this; }

PlainText  &PlainText ::operator << (PlainText   const &data) { append (data); return *this; }
PlainText  &PlainText ::operator << (PublicKey   const &data) { append (data); return *this; }
PlainText  &PlainText ::operator << (Nonce       const &data) { append (data); return *this; }
PlainText  &PlainText ::operator << (IPv4Address const &data) { append (data); return *this; }
PlainText  &PlainText ::operator << (IPv6Address const &data) { append (data); return *this; }

CipherText &CipherText::operator << (uint8_t            data) { append (data); return *this; }
CipherText &CipherText::operator << (uint16_t           data) { append (data); return *this; }
CipherText &CipherText::operator << (uint32_t           data) { append (data); return *this; }
CipherText &CipherText::operator << (uint64_t           data) { append (data); return *this; }

CipherText &CipherText::operator << (CipherText  const &data) { append (data); return *this; }
CipherText &CipherText::operator << (PublicKey   const &data) { append (data); return *this; }
CipherText &CipherText::operator << (Nonce       const &data) { append (data); return *this; }

void
PlainText::shift_left (std::size_t offset, std::size_t bit_size)
{
  assert (!empty ());
  assert (bit_size <= 8);

  if (offset == 0 && bit_size == 1)
    {
      assert ((at (size () - 1) & __extension__ 0b11111110) == 0);
      at (size () - 1) <<= 8 - bit_size;
    }
  else if (offset == 1 && bit_size == 7)
    {
      assert ((at (size () - 2) & __extension__ 0b01111111) == 0);
      at (size () - 2) |= at (size () - 1);
      pop_back ();
    }
  else
    {
      printf ("shift_left (%zd, %zd)\n", offset, bit_size);
      abort ();
    }
}



template<typename MessageFormat>
BitStream<MessageFormat>
BitStream<MessageFormat>::operator >> (uint8_t &b) const
{
  assert (position_ % 8 == 0);
#if 0
  printf ("reading %zd bits at %zd (of %zd)\n", 8, position_, packet_.size () * 8);
#endif
  assert (position_ / 8 < packet_.size ());
  b = packet_[position_ / 8 + 0] << 8 * 0;
#if 0
  printf ("byte at %zd: %02x\n", position_, b);
#endif
  return { position_ + 8, packet_ };
}

template<typename MessageFormat>
BitStream<MessageFormat>
BitStream<MessageFormat>::operator >> (uint16_t &b) const
{
  assert (position_ % 8 == 0);
#if 0
  printf ("reading %zd bits at %zd (of %zd)\n", 8, position_, packet_.size () * 8);
#endif
  assert (position_ / 8 + 1 < packet_.size ());
  b = 0;
  b |= packet_[position_ / 8 + 0] << 8 * 1;
  b |= packet_[position_ / 8 + 1] << 8 * 0;
#if 0
  printf ("byte at %zd: %02x\n", position_, b);
#endif
  return { position_ + 16, packet_ };
}

template<typename MessageFormat>
BitStream<MessageFormat>
BitStream<MessageFormat>::operator >> (uint32_t &b) const
{
  assert (position_ % 8 == 0);
#if 0
  printf ("reading %zd bits at %zd (of %zd)\n", 8, position_, packet_.size () * 8);
#endif
  assert (position_ / 8 + 3 < packet_.size ());
  b = 0;
  b |= packet_[position_ / 8 + 0] << 8 * 3;
  b |= packet_[position_ / 8 + 1] << 8 * 2;
  b |= packet_[position_ / 8 + 2] << 8 * 1;
  b |= packet_[position_ / 8 + 3] << 8 * 0;
#if 0
  printf ("byte at %zd: %02x\n", position_, b);
#endif
  return { position_ + 32, packet_ };
}

template<typename MessageFormat>
BitStream<MessageFormat>
BitStream<MessageFormat>::operator >> (uint64_t &b) const
{
  assert (position_ % 8 == 0);
#if 0
  printf ("reading %zd bits at %zd (of %zd)\n", 8, position_, packet_.size () * 8);
#endif
  assert (position_ / 8 + 7 < packet_.size ());
  b = 0;
  b |= uint64_t (packet_[position_ / 8 + 0]) << 8 * 7;
  b |= uint64_t (packet_[position_ / 8 + 1]) << 8 * 6;
  b |= uint64_t (packet_[position_ / 8 + 2]) << 8 * 5;
  b |= uint64_t (packet_[position_ / 8 + 3]) << 8 * 4;
  b |= uint64_t (packet_[position_ / 8 + 4]) << 8 * 3;
  b |= uint64_t (packet_[position_ / 8 + 5]) << 8 * 2;
  b |= uint64_t (packet_[position_ / 8 + 6]) << 8 * 1;
  b |= uint64_t (packet_[position_ / 8 + 7]) << 8 * 0;
#if 0
  printf ("byte at %zd: %02x\n", position_, b);
#endif
  return { position_ + 64, packet_ };
}


template<typename MessageFormat>
BitStream<MessageFormat>
BitStream<MessageFormat>::operator >> (MessageFormat &data) const
{
  data.resize (size ());
  read (data.data (), data.data () + data.size ());
  return { size () * 8, packet_ };
}


template<typename MessageFormat>
BitStream<MessageFormat>
BitStream<MessageFormat>::operator >> (PublicKey &data) const
{
  read (data.begin (), data.end ());
  return { position_ + data.size () * 8, packet_ };
}


template<typename MessageFormat>
BitStream<MessageFormat>
BitStream<MessageFormat>::operator >> (IPv4Address &data) const
{
  read (data.begin (), data.end ());
  return { position_ + data.size () * 8, packet_ };
}


template<typename MessageFormat>
BitStream<MessageFormat>
BitStream<MessageFormat>::operator >> (IPv6Address &data) const
{
  read (data.begin (), data.end ());
  return { position_ + data.size () * 8, packet_ };
}


template<typename MessageFormat>
BitStream<MessageFormat>
BitStream<MessageFormat>::read (uint8_t &b, std::size_t bit_size) const
{
#if 0
  printf ("reading %zd bits at %zd (of %zd)\n", bit_size, position_, packet_.size () * 8);
#endif
  assert (position_ / 8 < packet_.size ());
  if (position_ % 8 == 0 && bit_size == 1)
    b = packet_[position_ / 8] >> 7;
  else if (position_ % 8 == 1 && bit_size == 7)
    b = packet_[position_ / 8] & __extension__ 0b01111111;
  else
    {
      printf ("read at %zd, bit_size = %zd\n", position_, bit_size);
      abort ();
    }
#if 0
  printf ("byte at %zd: %02x\n", position_, b);
#endif
  return { position_ + bit_size, packet_ };
}


template struct tox::BitStream<PlainText>;
template struct tox::BitStream<CipherText>;
