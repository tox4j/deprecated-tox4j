#include <gtest/gtest.h>
#include <glog/logging.h>

int
main (int argc, char **argv)
{
  google::InitGoogleLogging (argv[0]);
  testing::InitGoogleTest (&argc, argv);

  google::LogToStderr ();

  return RUN_ALL_TESTS ();
}
