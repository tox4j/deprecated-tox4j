#
# This module defines the following variables:
#
# LIBTOX_FOUND - All required components and the core library were found
# LIBTOX_INCLUDE_DIRS - Combined list of all components include dirs
# LIBTOX_LIBRARIES - Combined list of all componenets libraries
#
# For each requested component the following variables are defined:
#
# LIBTOX_<component>_FOUND - The component was found
# LIBTOX_<component>_INCLUDE_DIRS - The components include dirs
# LIBTOX_<component>_LIBRARIES - The components libraries
#
# <component> is the uppercase name of the component
 
 
find_package(PkgConfig QUIET)
 
function(find_tox_library component header)
	string(TOUPPER "${component}" component_u)
	set(LIBTOX_${component_u}_FOUND FALSE PARENT_SCOPE)
	set(Libtox_${component}_FOUND FALSE PARENT_SCOPE)
	 
	if(PKG_CONFIG_FOUND)
		pkg_check_modules(PC_LIBTOX_${component} QUIET libtox${component})
	endif()
	 
	find_path(LIBTOX_${component}_INCLUDE_DIR
		NAMES
		"${header}"
		HINTS
		${PC_LIBTOX_${component}_INCLUDE_DIRS}
		PATH_SUFFIXES tox)
	 
	find_library(LIBTOX_${component}_LIBRARY
		NAMES
		"tox${component}" "libtox${component}"
		HINTS
		${PC_LIBTOX_${component}_LIBRARY_DIRS})
	 
	set(LIBTOX_${component_u}_INCLUDE_DIRS ${LIBTOX_${component}_INCLUDE_DIR} PARENT_SCOPE)
	set(LIBTOX_${component_u}_LIBRARIES ${LIBTOX_${component}_LIBRARY} PARENT_SCOPE)
	 
	mark_as_advanced(LIBTOX_${component}_INCLUDE_DIR LIBTOX_${component}_LIBRARY)
	 
	if(LIBTOX_${component}_INCLUDE_DIR AND LIBTOX_${component}_LIBRARY)
		set(LIBTOX_${component_u}_FOUND TRUE PARENT_SCOPE)
		set(Libtox_${component}_FOUND TRUE PARENT_SCOPE)
		 
		list(APPEND LIBTOX_INCLUDE_DIRS ${LIBTOX_${component}_INCLUDE_DIR})
		list(REMOVE_DUPLICATES LIBTOX_INCLUDE_DIRS)
		set(LIBTOX_INCLUDE_DIRS "${LIBTOX_INCLUDE_DIRS}" PARENT_SCOPE)
		 
		list(APPEND LIBTOX_LIBRARIES ${LIBTOX_${component}_LIBRARY})
		list(REMOVE_DUPLICATES LIBTOX_LIBRARIES)
		set(LIBTOX_LIBRARIES "${LIBTOX_LIBRARIES}" PARENT_SCOPE)
	endif()
endfunction()
 
set(LIBTOX_INCLUDE_DIRS)
set(LIBTOX_LIBRARIES)
 
if(NOT Libtox_FIND_COMPONENTS)
	message(FATAL_ERROR "No Libtox componenets requested")
endif()
 
list(GET Libtox_FIND_COMPONENTS 0 _first_comp)
string(TOUPPER "${_first_comp}" _first_comp)
 
foreach(component ${Libtox_FIND_COMPONENTS})
	if(component STREQUAL "core")
		find_tox_library("${component}" "tox.h")
	elseif(component STREQUAL "dns")
		find_tox_library("${component}" "toxdns.h")
	elseif(component STREQUAL "encryptsave")
		find_tox_library("${component}" "toxencryptsave.h")
	elseif(component STREQUAL "av")
		find_tox_library("${component}" "toxav.h")
	else()
		message(FATAL_ERROR "Unknown Libtox component requested: ${component}")
	endif()
endforeach()
 
include(FindPackageHandleStandardArgs)
find_package_handle_standard_args(Libtox
	FOUND_VAR LIBTOX_FOUND
	REQUIRED_VARS LIBTOX_${_first_comp}_LIBRARIES LIBTOX_${_first_comp}_INCLUDE_DIRS
	HANDLE_COMPONENTS) 
