#pragma once

#include <string>

#include "KeyPair.h"
#include "Nonce.h"

namespace tox
{
  template<typename MessageFormat>
  struct Message;

  template<typename MessageFormat, typename Underlying>
  struct MessageIterator
  {
    friend struct Message<MessageFormat>;

    typedef typename std::iterator_traits<Underlying>::difference_type difference_type;
    typedef typename std::iterator_traits<Underlying>::pointer pointer;
    typedef typename std::iterator_traits<Underlying>::reference reference;
    typedef typename std::iterator_traits<Underlying>::value_type value_type;
    typedef typename std::iterator_traits<Underlying>::iterator_category iterator_category;

    friend bool operator== (MessageIterator lhs, MessageIterator rhs)
    {
      return lhs.underlying_ == rhs.underlying_;
    }

    friend bool operator!= (MessageIterator lhs, MessageIterator rhs)
    {
      return lhs.underlying_ != rhs.underlying_;
    }

    friend difference_type operator- (MessageIterator lhs, MessageIterator rhs)
    {
      return lhs.underlying_ - rhs.underlying_;
    }

    friend MessageIterator operator+ (MessageIterator lhs, difference_type offset)
    {
      return MessageIterator (lhs.underlying_ + offset);
    }

    friend MessageIterator operator- (MessageIterator lhs, difference_type offset)
    {
      return MessageIterator (lhs.underlying_ - offset);
    }

    reference operator * () const { return *underlying_; }

    MessageIterator &operator ++ () { ++underlying_; return *this; }
    MessageIterator &operator += (difference_type offset) { underlying_ += offset; return *this; }
    MessageIterator &operator -= (difference_type offset) { underlying_ -= offset; return *this; }

  protected:
    explicit MessageIterator (Underlying underlying)
      : underlying_ (underlying)
    {
    }

  private:
    Underlying underlying_;
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
    : protected byte_vector
  {
    /**
     * Only const_iterator is allowed. Mutation via iterators is dangerous,
     * since it allows algorithms to bypass static message format checking.
     */
    typedef MessageIterator<MessageFormat, byte_vector::const_iterator> const_iterator;

    using byte_vector::data;
    using byte_vector::size;
    using byte_vector::push_back;

    const_iterator  begin () const { return const_iterator (byte_vector:: begin ()); }
    const_iterator  end   () const { return const_iterator (byte_vector:: end   ()); }
    const_iterator cbegin () const { return const_iterator (byte_vector::cbegin ()); }
    const_iterator cend   () const { return const_iterator (byte_vector::cend   ()); }


    /**
     * Constructors.
     */
    Message ()
    { }

    Message (size_t initial)
      : byte_vector (initial)
    { }

    Message (const_iterator first, const_iterator last)
      : byte_vector (first, last)
    { }


  protected:
    template<typename InputIt>
    Message (InputIt first, InputIt last)
      : byte_vector (first, last)
    {
    }
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

    PlainText &operator << (byte b);
    PlainText &operator << (PlainText const &plain);
    PlainText &operator << (PublicKey const &key);
    PlainText &operator << (Nonce const &nonce);

  private:
    template<typename InputIt>
    void append (InputIt first, InputIt last)
    {
      return byte_vector::insert (byte_vector::end (), first, last);
    }
  };

  struct CipherText
    : Message<CipherText>
  {
    using Message<CipherText>::Message;
  };
}
