#include "lwt/variant.h"
#include <gtest/gtest.h>



TEST (Variant, Dispatch) {
  variant<int, char, std::string> s1 (std::string ("hello"));
  //variant<int, char, std::string> s2 ("hello");

  struct v
  {
    char const *operator () (int) const { return "int"; }
    char const *operator () (char) const { return "char"; }
    char const *operator () (std::string) const { return "std::string"; }
  };

  EXPECT_EQ ("std::string", s1 (v ()));
}


TEST (Variant, StringValue) {
  variant<int, char, std::string> s1 (std::string ("hello"));

  struct v
  {
    std::string operator () (int) const { return "int"; }
    std::string operator () (char) const { return "char"; }
    std::string operator () (std::string s) const { return s; }
  };

  EXPECT_EQ ("hello", s1 (v ()));
}


TEST (Variant, MoveStringValue) {
  variant<int, char, std::string> s1 (std::string ("hello"));

  auto s2 = std::move (s1);

  struct v
  {
    std::string operator () (int) const { return "int"; }
    std::string operator () (char) const { return "char"; }
    std::string operator () (std::string s) const { return s; }
  };

  EXPECT_EQ ("", s1 (v ()));
  EXPECT_EQ ("hello", s2 (v ()));
}


TEST (Variant, Copy) {
  variant<int, char, std::string> s1 (std::string ("hello"));

  auto s2 = s1;

  struct v
  {
    char const *operator () (int) const { return "int"; }
    char const *operator () (char) const { return "char"; }
    char const *operator () (std::string) const { return "std::string"; }
  };

  EXPECT_EQ ("std::string", s1 (v ()));
  EXPECT_EQ ("std::string", s2 (v ()));
}


TEST (Variant, Move) {
  variant<int, char, std::string> s1 (std::string ("hello"));

  auto s2 = std::move (s1);

  struct v
  {
    char const *operator () (int) const { return "int"; }
    char const *operator () (char) const { return "char"; }
    char const *operator () (std::string) const { return "std::string"; }
  };

  EXPECT_EQ ("std::string", s2 (v ()));
}


TEST (Variant, MoveInt) {
  variant<int, char, std::string> s1 (3);

  auto s2 = std::move (s1);

  struct v
  {
    char const *operator () (int) const { return "int"; }
    char const *operator () (char) const { return "char"; }
    char const *operator () (std::string) const { return "std::string"; }
  };

  EXPECT_EQ ("int", s2 (v ()));
}


TEST (Variant, Visitor) {
  variant<int, char, std::string> s (3);

  auto result = s.visit<char const *> () >>= {
    [] (int) { return "int"; },
    [] (char) { return "char"; },
    [] (std::string) { return "std::string"; },
  };

  EXPECT_EQ ("int", result);
}


TEST (Variant, DefaultConstructed) {
  variant<int, char, std::string> s;

  EXPECT_TRUE (s.empty ());

  s = 3;

  auto result = s.visit<char const *> () >>= {
    [] (int) { return "int"; },
    [] (char) { return "char"; },
    [] (std::string) { return "std::string"; },
  };

  EXPECT_EQ ("int", result);

  s = std::string ("3");

  result = s.visit<char const *> () >>= {
    [] (int) { return "int"; },
    [] (char) { return "char"; },
    [] (std::string) { return "std::string"; },
  };

  EXPECT_EQ ("std::string", result);
}
