#pragma once

#include <string>

#include "types.h"

namespace tox
{
  template<typename MessageFormat, bool Default = false>
  struct allowed_types
  {
    typedef std::tuple<
      MessageFormat,
      uint8_t,
      uint16_t,
      uint32_t,
      uint64_t,
      PublicKey,
      Nonce
    > type;
  };

  template<>
  struct allowed_types<PlainText>
    : tuple_append<typename allowed_types<PlainText, true>::type,
        IPv4Address,
        IPv6Address
      >
  { };

  template<>
  struct allowed_types<CipherText>
    : tuple_append<typename allowed_types<CipherText, true>::type
        // Nothing more.
      >
  { };

  template<typename ValueType, typename MessageFormat>
  struct is_allowed_in
    : is_in_tuple<ValueType, typename allowed_types<MessageFormat>::type>
  { };


  template<typename MessageFormat, typename Underlying>
  struct MessageIterator
  {
    friend struct Message<MessageFormat>;

    typedef typename std::iterator_traits<Underlying>::difference_type difference_type;
    typedef typename std::iterator_traits<Underlying>::pointer pointer;
    typedef typename std::iterator_traits<Underlying>::reference reference;
    typedef typename std::iterator_traits<Underlying>::value_type value_type;
    typedef typename std::iterator_traits<Underlying>::iterator_category iterator_category;

#define BINARY_OPERATOR(TYPE, OP)							\
    friend TYPE operator OP (MessageIterator lhs, MessageIterator rhs)			\
    {											\
      return lhs.underlying_ OP rhs.underlying_;					\
    }

    BINARY_OPERATOR (bool, < )
    BINARY_OPERATOR (bool, <=)
    BINARY_OPERATOR (bool, > )
    BINARY_OPERATOR (bool, >=)
    BINARY_OPERATOR (bool, ==)
    BINARY_OPERATOR (bool, !=)
    BINARY_OPERATOR (difference_type, -)
#undef BINARY_OPERATOR

#define BINARY_OPERATOR(OP)								\
    friend MessageIterator operator OP (MessageIterator lhs, difference_type offset)	\
    {											\
      return MessageIterator (lhs.underlying_ OP offset);				\
    }

    BINARY_OPERATOR (+)
    BINARY_OPERATOR (-)
#undef BINARY_OPERATOR

#define BINARY_OPERATOR(OP)								\
    friend MessageIterator &operator OP (MessageIterator &lhs, difference_type offset)	\
    {											\
      lhs.underlying_ OP offset;							\
      return lhs;									\
    }

    BINARY_OPERATOR (+=)
    BINARY_OPERATOR (-=)
#undef BINARY_OPERATOR

    MessageIterator &operator ++ () { ++underlying_; return *this; }
    MessageIterator &operator -- () { --underlying_; return *this; }

    reference  operator *  () const { return *underlying_; }
    Underlying operator -> () const { return  underlying_; }

  protected:
    explicit MessageIterator (Underlying underlying)
      : underlying_ (underlying)
    {
    }

  private:
    Underlying underlying_;
  };

  struct MessageBase
    : protected byte_vector
  {
    using byte_vector::data;
    using byte_vector::size;
    using byte_vector::push_back;

    /**
     * Constructors.
     */
    MessageBase ()
    { }

    MessageBase (std::size_t initial)
      : byte_vector (initial)
    { }

  protected:
    template<typename InputIt>
    MessageBase (InputIt first, InputIt last)
      : byte_vector (first, last)
    {
    }

    void append (uint8_t  b);
    void append (uint16_t s);
    void append (uint32_t l);
    void append (uint64_t q);

    template<typename Iterable>
    void append (Iterable const &data)
    {
      append (data.cbegin (), data.cend ());
    }

    template<typename InputIt>
    void append (InputIt first, InputIt last)
    {
      return byte_vector::insert (byte_vector::end (), first, last);
    }
  };

  /**
   * Type for message data.
   *
   * The @a Format parameter is used to ensure that no information flows
   * directly from plain text data to cipher text data, and vice versa, unless
   * done through explicit conversions through @c byte_vector. This is done by
   * the @c CryptoBox::encrypt function.
   */
  template<typename MessageFormat>
  struct Message
    : MessageBase
  {
    using MessageBase::MessageBase;

    friend struct BitStream<MessageFormat>;

    /**
     * Only const_iterator is allowed. Mutation via iterators is dangerous,
     * since it allows algorithms to bypass static message format checking.
     */
    typedef MessageIterator<MessageFormat, byte_vector::const_iterator> const_iterator;

    const_iterator  begin () const { return const_iterator (byte_vector:: begin ()); }
    const_iterator  end   () const { return const_iterator (byte_vector:: end   ()); }
    const_iterator cbegin () const { return const_iterator (byte_vector::cbegin ()); }
    const_iterator cend   () const { return const_iterator (byte_vector::cend   ()); }

    template<typename T>
    MessageFormat &operator << (T const &data)
    {
      static_assert (is_allowed_in<T, MessageFormat>::value,
                     "Data type is not allowed in this packet type");
      append (data);
      return static_cast<MessageFormat &> (*this);
    }

    Message (const_iterator first, const_iterator last)
      : MessageBase (first, last)
    { }

  protected:
    Message ()
    { }
  };


  struct PlainText
    : Message<PlainText>
  {
    using Message<PlainText>::Message;

    static PlainText from_bytes (byte_vector const &bytes)
    {
      return PlainText (bytes.begin (), bytes.end ());
    }

    static PlainText from_string (std::string const &str)
    {
      return PlainText (str.begin (), str.end ());
    }

    void shift_left (std::size_t offset, std::size_t bit_size);
  };

  struct CipherText
    : Message<CipherText>
  {
    using Message<CipherText>::Message;

    struct BitStream;

    static CipherText from_bytes (byte const *bytes, std::size_t length)
    {
      return CipherText (bytes, bytes + length);
    }

    static CipherText from_bytes (byte_vector const &bytes, std::size_t length)
    {
      assert (bytes.size () >= length);
      return CipherText (bytes.begin (), bytes.begin () + length);
    }
  };

  template<typename MessageFormat>
  struct BitStream
  {
    BitStream (MessageFormat const &packet)
      : BitStream (0, packet)
    { }

    std::size_t size () const { return packet_.size () - position_ / 8; }
    typename MessageFormat::const_iterator cbegin () const { return packet_.cbegin () + position_ / 8; }
    typename MessageFormat::const_iterator cend   () const { return packet_.cend   (); }

    BitStream read (uint8_t &b, std::size_t bit_size) const;

    template<typename OutputIterator>
    BitStream read (OutputIterator first, OutputIterator last) const
    {
      typename std::iterator_traits<OutputIterator>::difference_type size =
        std::distance (first, last);

      assert (position_ % 8 == 0);
      assert (packet_.size () >= position_ / 8 + size);
      std::copy (cbegin (),
                 cbegin () + size,
                 first);
      return { position_ + size * 8, packet_ };
    }

    BitStream operator >> (uint8_t  &b) const;
    BitStream operator >> (uint16_t &s) const;
    BitStream operator >> (uint32_t &l) const;
    BitStream operator >> (uint64_t &q) const;

    BitStream operator >> (MessageFormat &message) const;

    template<std::size_t BitSize>
    struct with_bit_size
    {
      with_bit_size (BitStream const &stream)
        : stream_ (stream)
      { }

      template<typename T>
      BitStream operator >> (T &v) const
      {
        return stream_.read (v, BitSize);
      }

    private:
      BitStream const stream_;
    };

    template<std::size_t BitSize>
    with_bit_size<BitSize> bit_size () const
    {
      return with_bit_size<BitSize> (*this);
    }

  private:
    BitStream (std::size_t position, MessageFormat const &packet)
      : position_ (position)
      , packet_ (packet)
    { }

    std::size_t const position_;
    MessageFormat const &packet_;
  };
}
