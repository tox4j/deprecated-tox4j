#include "tox/core/variant.h"
#include <gtest/gtest.h>



TEST (Variant, Dispatch) {
  variant<int, char, std::string> s (std::string ("hello"));
  //variant<int, char, std::string> s2 ("hello");

  struct v
  {
    char const *operator () (int i) const { return "int"; }
    char const *operator () (char c) const { return "char"; }
    char const *operator () (std::string s) const { return "std::string"; }
  };

  EXPECT_EQ ("std::string", s (v ()));
}
