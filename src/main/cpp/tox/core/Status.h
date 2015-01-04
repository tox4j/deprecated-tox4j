#pragma once

namespace tox
{
  enum class Status
  {
    OK,
    Unknown = 1000,
    HMACError,
    Failure,
    FormatError,
  };
}
