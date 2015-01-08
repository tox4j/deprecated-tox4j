#include "lwt/io.h"
#include "tox/core/Logging.h"
#include <gtest/gtest.h>

using lwt::io;


TEST (IO, Read) {
  auto result =
    lwt::open ("/dev/random")

    ->* [](int fd) {
      return lwt::read (fd, 10);
    }

    ->* [](std::vector<uint8_t> const &data) {
      return lwt::success (data.size ());
    };

#if 0
  result->eval ([](std::size_t size) {
    EXPECT_EQ (10, size);
  });
#endif
}
