#include "lwt/io.h"
#include "lwt/logging.h"
#include <gtest/gtest.h>

using lwt::io;


TEST (IO, Read) {
  LOG (INFO) << "opening /dev/random";

  auto result =
    lwt::open ("/dev/random")

    ->* [](int fd) {
      LOG (INFO) << "reading";
      return lwt::read (fd, 10);
    }

    ->* [](std::vector<uint8_t> const &data) {
      LOG (INFO) << "got " << data.size () << " bytes";
      return lwt::success (data.size ());
    }

    ->* [](std::size_t size) {
      LOG (INFO) << "passed value was " << size;
      EXPECT_EQ (10, size);
      return lwt::success ();
    };

  lwt::eval (result);
}
