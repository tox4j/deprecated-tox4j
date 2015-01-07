#pragma once

namespace tox
{
  enum class Status
  {
    Unknown = 1000,
    HMACError,
    Failure,
    FormatError,
  };
}
