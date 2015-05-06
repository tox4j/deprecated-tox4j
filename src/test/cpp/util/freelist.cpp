#include "util/freelist.h"

#include "tox/logging.h"
#include <gtest/gtest.h>


TEST (Freelist, Destroy) {
  synchronised_freelist<int> fl;
  std::size_t index = fl.add (std::unique_ptr<int> (new int (123)));
  EXPECT_TRUE (fl.destroy (index));
}


TEST (Freelist, DestroyTwice) {
  synchronised_freelist<int> fl;
  std::size_t index = fl.add (std::unique_ptr<int> (new int (123)));
  EXPECT_TRUE (fl.destroy (index));
  EXPECT_FALSE (fl.destroy (index));
  EXPECT_FALSE (fl.destroy (index));
}


TEST (Freelist, Finalize) {
  synchronised_freelist<int> fl;
  std::size_t index = fl.add (std::unique_ptr<int> (new int (123)));
  EXPECT_TRUE (fl.destroy (index));
  EXPECT_TRUE (fl.finalize (index));
}


TEST (Freelist, FinalizeTwice) {
  synchronised_freelist<int> fl;
  std::size_t index = fl.add (std::unique_ptr<int> (new int (123)));
  EXPECT_TRUE (fl.destroy (index));
  EXPECT_TRUE (fl.finalize (index));
  EXPECT_FALSE (fl.finalize (index));
  EXPECT_FALSE (fl.finalize (index));
}


TEST (Freelist, DestroyNoReassign) {
  synchronised_freelist<int> fl;
  std::size_t index1 = fl.add (std::unique_ptr<int> (new int (123)));
  EXPECT_TRUE (fl.destroy (index1));
  std::size_t index2 = fl.add (std::unique_ptr<int> (new int (321)));
  EXPECT_NE (index1, index2);
  EXPECT_TRUE (fl.destroy (index2));
}


TEST (Freelist, FinalizeReassign) {
  synchronised_freelist<int> fl;
  std::size_t index1 = fl.add (std::unique_ptr<int> (new int (123)));
  EXPECT_TRUE (fl.destroy (index1));
  EXPECT_TRUE (fl.finalize (index1));
  std::size_t index2 = fl.add (std::unique_ptr<int> (new int (321)));
  EXPECT_EQ (index1, index2);
  EXPECT_TRUE (fl.destroy (index2));
  EXPECT_TRUE (fl.finalize (index2));
}
