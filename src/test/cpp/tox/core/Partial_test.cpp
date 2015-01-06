#include "tox/core/Partial.h"
#include "tox/core/Logging.h"
#include <gtest/gtest.h>

using namespace tox;


TEST (Partial, String) {
  Partial<std::string> result = success ("hello");
  
  EXPECT_TRUE (result.ok ());
  result >> [](std::string const &value) {
    EXPECT_EQ ("hello", value);
    return success ();
  };
}


TEST (Partial, Copy) {
  Partial<std::string> result1 = success ("hello");
  Partial<std::string> result2 = result1;
  
  EXPECT_TRUE (result1.ok ());
  result1 >> [](std::string const &value) {
    EXPECT_EQ ("hello", value);
    return success ();
  };

  EXPECT_TRUE (result2.ok ());
  result2 >> [](std::string const &value) {
    EXPECT_EQ ("hello", value);
    return success ();
  };
}


TEST (Partial, Move) {
  Partial<std::string> result1 = success ("hello");
  Partial<std::string> result2 = std::move (result1);
  
  EXPECT_TRUE (result1.ok ());
  result1 >> [](std::string const &value) {
    EXPECT_EQ ("", value);
    return success ();
  };

  EXPECT_TRUE (result2.ok ());
  result2 >> [](std::string const &value) {
    EXPECT_EQ ("hello", value);
    return success ();
  };
}
