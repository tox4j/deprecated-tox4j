#include "Message.h"
#include "Logging.h"

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
  printf ("reading %zd bits at %zd (of %zd)\n", 8, position_, packet_.size () * 8);
  assert (position_ / 8 < packet_.size ());
  b = packet_[position_ / 8];
  printf ("byte at %zd: %02x\n", position_, b);
  return { position_ + 8, packet_ };
}


template<typename MessageFormat>
BitStream<MessageFormat>
BitStream<MessageFormat>::operator >> (MessageFormat &message) const
{
  assert (position_ % 8 == 0);
  message.clear ();
  message.append (packet_.begin () + position_ / 8,
                  packet_.end ());
  return { size () * 8, packet_ };
}


template<typename MessageFormat>
BitStream<MessageFormat>
BitStream<MessageFormat>::read (uint8_t &b, std::size_t bit_size) const
{
  printf ("reading %zd bits at %zd (of %zd)\n", bit_size, position_, packet_.size () * 8);
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
  printf ("byte at %zd: %02x\n", position_, b);
  return { position_ + bit_size, packet_ };
}


template struct tox::BitStream<PlainText>;
template struct tox::BitStream<CipherText>;
