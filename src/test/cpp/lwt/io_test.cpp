#include "lwt/event_loop.h"
#include <gtest/gtest.h>


using namespace lwt;


TEST (IO, OpenClose) {
  io<> program = open ("/dev/stdin")
    ->* [] (shared_fd fd) -> io<> {
      LOG_ASSERT (fd->fd >= 0);
      return success ();
    };

  run (program);
}


TEST (IO, Read) {
  typedef std::vector<uint8_t> byte_vec;

  io<> program = open ("/dev/stdin")

    ->* [] (shared_fd fd) -> io<byte_vec> {
      return read (fd, 10);
    }

    ->* [] (byte_vec const &data) -> io<> {
      LOG_ASSERT (data.size () == 10);
      return success ();
    };

  run (program);
}


TEST (IO, DirectFailure) {
  typedef std::vector<uint8_t> byte_vec;

  io<> program = open ("/dev/stdin")

    ->* [] (shared_fd fd) -> io<byte_vec> {
      LOG_ASSERT (fd->fd >= 0);
      return failure (SystemError (0));
    }

    ->* [] (byte_vec const &data) -> io<> {
      LOG_ASSERT (data.size () == 10);
      return success ();
    };

  run (program);
}


TEST (IO, WaitingFailure) {
  typedef std::vector<uint8_t> byte_vec;

  io<> program = open ("/dev/stdin")

    ->* [] (shared_fd fd) -> io<byte_vec> {
      return read (fd, 10)
        ->* [] (byte_vec const &data) -> io<byte_vec> {
          LOG (INFO) << "Got " << data.size () << " bytes";
          LOG_ASSERT (data.size () == 10);
          return failure (SystemError (0));
        };
    }

    ->* [] (byte_vec const &data) -> io<> {
      LOG_ASSERT (data.size () == 10);
      return success ();
    };

  run (program);
}


TEST (IO, ReadMultiplex) {
  typedef std::vector<uint8_t> byte_vec;

  io<> program = open ("/dev/stdin")
    ->* [] (shared_fd fd) -> io<> {
      io<byte_vec> waiting_read = read (fd, 10);

      // First waiting operation on first read.
      io<byte_vec> waiting1_1 = waiting_read
        ->* [] (byte_vec const &buffer1) -> io<> {
          SCOPE;
          LOG (INFO) << "got buffer 1: " << buffer1.size ();
          return success ();
        }

        ->* deferred (lwt::read, fd, 10);

      // First waiting operation on second read.
      io<> waiting2_1 = waiting1_1
        ->* [] (byte_vec const &buffer2) -> io<> {
          SCOPE;
          LOG (INFO) << "got buffer 2: " << buffer2.size ();
          return success ();
        };


      // Second waiting operation on second read.
      io<> waiting2_2 = waiting1_1
        ->* [] (byte_vec const &buffer2) -> io<> {
          SCOPE;
          LOG (INFO) << "got buffer 2 again: " << buffer2.size ();
          return success ();
        };


      // Second waiting operation on first read.
      io<> waiting1_2 = waiting_read
        ->* [] (byte_vec const &buffer1) -> io<> {
          SCOPE;
          LOG (INFO) << "got buffer 1 again: " << buffer1.size ();
          return success ();
        };

      return combine<SystemError> (waiting2_1, waiting2_2, waiting1_2);
    };

  run (program);
}
