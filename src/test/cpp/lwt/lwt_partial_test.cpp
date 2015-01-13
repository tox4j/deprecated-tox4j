#include "lwt/partial.h"
#include "lwt/logging.h"
#include <gtest/gtest.h>


enum class Status
{
  Unknown,
};

DEFINE_PARTIAL_TYPE (Partial, Status, Status::Unknown)


TEST (Lwt, IntSuccess) {
  Partial<int> v = success (3);
  EXPECT_TRUE (v.ok ());
}


TEST (Lwt, IntFailure) {
  Partial<int> v = failure ();
  EXPECT_FALSE (v.ok ());
}


TEST (Lwt, Conversion1) {
  Partial<int> v = success (3.0);
  v.bind ([](int i) {
    EXPECT_EQ (3, i);
  });
}


TEST (Lwt, Conversion2) {
  Partial<std::string> v = success ("3");
  v ->* [](std::string i) {
    EXPECT_EQ ("3", i);
  };
}


TEST (Lwt, Conversion3) {
  Partial<std::string> v1 = failure ();
  Partial<int> v2 = v1 ->* []{
    return failure ();
  };
  EXPECT_FALSE (v1.ok ());
  EXPECT_FALSE (v2.ok ());
}


TEST (Lwt, Move1) {
  std::string hi ("hi");
  Partial<std::string> v1 = success (hi);
  EXPECT_EQ ("hi", hi);

  Partial<std::string> v2 = success (std::move (hi));
  EXPECT_EQ ("", hi);

  Partial<int> v3 = v2 ->* []{
    return failure ();
  };
  EXPECT_TRUE (v1.ok ());
  EXPECT_TRUE (v2.ok ());
  EXPECT_FALSE (v3.ok ());
}


TEST (Lwt, Bind1) {
  Partial<int> v = failure ();
  v ->* []{
    EXPECT_TRUE (false);
  };
}


TEST (Lwt, Bind2) {
  Partial<int> v = success (3);
  v.bind ([](int i) {
    EXPECT_EQ (3, i);
  });
}


TEST (Lwt, Map1) {
  Partial<int> v = success (3);
  auto r = v.map ([](int i) {
    EXPECT_EQ (3, i);
    return i + 1;
  }).bind ([](int i) {
    EXPECT_EQ (4, i);
    return success ();
  }).bind ([] {
    return success ();
  });
  EXPECT_TRUE (r.ok ());
}


TEST (Lwt, Map2) {
  Partial<int> v = success (3);
  auto r = v.map ([](int i) {
    EXPECT_EQ (3, i);
    return i + 1;
  }) ->* [](int i) {
    EXPECT_EQ (4, i);
    return success ();
  } ->* [] {
    return success ();
  };
  EXPECT_TRUE (r.ok ());
}
